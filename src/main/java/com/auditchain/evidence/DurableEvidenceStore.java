package com.auditchain.evidence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * File-backed SQLite store for evidence submissions so idempotency survives restarts.
 */
public class DurableEvidenceStore implements AutoCloseable {

    private final Connection connection;

    protected DurableEvidenceStore(boolean initialize) {
        if (initialize) throw new IllegalArgumentException("Use path constructor for SQLite");
        this.connection = null;
    }

    public DurableEvidenceStore(Path dbPath) {
        try {
            Path parent = dbPath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
            this.connection.setAutoCommit(true);
            initSchema();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open evidence store", e);
        }
    }

    public DurableEvidenceStore() {
        this(Path.of("data", "evidence.db"));
    }

    private void initSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS evidence_submission (
                id                   TEXT PRIMARY KEY,
                idempotency_key      TEXT NOT NULL UNIQUE,
                actor_id             TEXT NOT NULL,
                location_code        TEXT,
                evidence_fingerprint TEXT NOT NULL,
                content_type         TEXT,
                size_bytes           INTEGER NOT NULL DEFAULT 0,
                payload_json         TEXT,
                submitted_at         TEXT NOT NULL,
                chain_entry_id       TEXT,
                receipt_hash         TEXT NOT NULL,
                evidence_bytes       BLOB
            );
            CREATE INDEX IF NOT EXISTS idx_evidence_idempotency ON evidence_submission(idempotency_key);
            CREATE TABLE IF NOT EXISTS review_item (
                id                      TEXT PRIMARY KEY,
                evidence_id             TEXT NOT NULL,
                vision_notes            TEXT,
                arithmetic_consistent   INTEGER NOT NULL DEFAULT 0,
                requires_human_review   INTEGER NOT NULL DEFAULT 1,
                decision                TEXT NOT NULL DEFAULT 'PENDING',
                reviewer_id             TEXT,
                decided_at              TEXT,
                reviewer_notes          TEXT,
                enqueued_at             TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_review_pending ON review_item(decision);
            """;
        try (Statement st = connection.createStatement()) {
            for (String s : sql.split(";")) {
                if (!s.isBlank()) st.executeUpdate(s);
            }
        }
        boolean hasBytes = false;
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(evidence_submission)")) {
            while (rs.next()) if ("evidence_bytes".equalsIgnoreCase(rs.getString("name"))) hasBytes = true;
        }
        if (!hasBytes) try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE evidence_submission ADD COLUMN evidence_bytes BLOB");
        }
    }

    public Optional<EvidenceSubmission> findByIdempotency(String key) {
        String sql = "SELECT * FROM evidence_submission WHERE idempotency_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapEvidence(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return Optional.empty();
    }

    public void save(EvidenceSubmission sub) {
        String sql = """
            INSERT OR REPLACE INTO evidence_submission
            (id, idempotency_key, actor_id, location_code, evidence_fingerprint, content_type,
             size_bytes, payload_json, submitted_at, chain_entry_id, receipt_hash, evidence_bytes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sub.getId().toString());
            ps.setString(2, sub.getIdempotencyKey());
            ps.setString(3, sub.getActorId());
            ps.setString(4, sub.getLocationCode());
            ps.setString(5, sub.getEvidenceFingerprint());
            ps.setString(6, sub.getContentType());
            ps.setLong(7, sub.getSizeBytes());
            ps.setString(8, sub.getPayloadJson());
            ps.setString(9, sub.getSubmittedAt().toString());
            ps.setString(10, sub.getChainEntryId() != null ? sub.getChainEntryId().toString() : null);
            ps.setString(11, sub.getReceiptHash());
            ps.setBytes(12, sub.getEvidenceBytes());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save evidence", e);
        }
    }

    public java.util.List<java.util.Map<String, Object>> loadReviews() {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT id,evidence_id,vision_notes,arithmetic_consistent,requires_human_review,decision,reviewer_id,decided_at,reviewer_notes,enqueued_at FROM review_item ORDER BY enqueued_at";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("id", rs.getString("id")); row.put("evidenceId", rs.getString("evidence_id"));
                row.put("notes", rs.getString("vision_notes")); row.put("arithmeticConsistent", rs.getInt("arithmetic_consistent") != 0);
                row.put("requiresHumanReview", rs.getInt("requires_human_review") != 0); row.put("decision", rs.getString("decision"));
                row.put("reviewerId", rs.getString("reviewer_id")); row.put("decidedAt", rs.getString("decided_at"));
                row.put("reviewerNotes", rs.getString("reviewer_notes")); row.put("enqueuedAt", rs.getString("enqueued_at"));
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) { throw new IllegalStateException("Failed to load review queue", e); }
    }

    public void saveReview(String id, String evidenceId, String notes, boolean arithmeticOk,
                           boolean needsReview, String decision, String enqueuedAt) {
        String sql = """
            INSERT OR REPLACE INTO review_item
            (id, evidence_id, vision_notes, arithmetic_consistent, requires_human_review,
             decision, enqueued_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, evidenceId);
            ps.setString(3, notes);
            ps.setInt(4, arithmeticOk ? 1 : 0);
            ps.setInt(5, needsReview ? 1 : 0);
            ps.setString(6, decision);
            ps.setString(7, enqueuedAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save review item", e);
        }
    }

    public void updateReviewDecision(String id, String decision, String reviewerId, String notes, String decidedAt) {
        String sql = "UPDATE review_item SET decision=?, reviewer_id=?, reviewer_notes=?, decided_at=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, decision);
            ps.setString(2, reviewerId);
            ps.setString(3, notes);
            ps.setString(4, decidedAt);
            ps.setString(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private EvidenceSubmission mapEvidence(ResultSet rs) throws SQLException {
        EvidenceSubmission e = new EvidenceSubmission();
        e.setId(UUID.fromString(rs.getString("id")));
        e.setIdempotencyKey(rs.getString("idempotency_key"));
        e.setActorId(rs.getString("actor_id"));
        e.setLocationCode(rs.getString("location_code"));
        e.setEvidenceFingerprint(rs.getString("evidence_fingerprint"));
        e.setContentType(rs.getString("content_type"));
        e.setSizeBytes(rs.getLong("size_bytes"));
        e.setPayloadJson(rs.getString("payload_json"));
        e.setSubmittedAt(Instant.parse(rs.getString("submitted_at")));
        String cid = rs.getString("chain_entry_id");
        if (cid != null) e.setChainEntryId(UUID.fromString(cid));
        e.setReceiptHash(rs.getString("receipt_hash"));
        e.setEvidenceBytes(rs.getBytes("evidence_bytes"));
        return e;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}

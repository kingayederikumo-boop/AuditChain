package com.auditchain.evidence;

import com.auditchain.core.HashChainEngine;
import com.auditchain.model.AuditEntry;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** JDBC-backed evidence and review persistence using the existing Flyway V2 schema. */
public class PostgresEvidenceStore extends DurableEvidenceStore {
    private static final long AUDIT_CHAIN_ADVISORY_LOCK = 782347123L;
    private final DataSource dataSource;

    public PostgresEvidenceStore(DataSource dataSource) {
        super(false);
        this.dataSource = dataSource;
    }

    /**
     * Insert the evidence receipt and its audit-chain entry in one PostgreSQL transaction.
     * The transaction-scoped advisory lock serializes this operation with other chain appends.
     */
    public EvidenceSubmission saveAtomically(String chainPayload, String actorId,
                                              String locationCode, EvidenceSubmission sub) {
        String insertAudit = """
            INSERT INTO audit_entry
            (id, sequence_number, previous_hash, payload, actor_role, actor_id,
             timestamp, current_hash, signature, location_code, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String insertEvidence = """
            INSERT INTO evidence_submission
            (id,idempotency_key,actor_id,location_code,evidence_fingerprint,content_type,
             size_bytes,payload_json,submitted_at,chain_entry_id,receipt_hash,evidence_bytes)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement lock = conn.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
                    lock.setLong(1, AUDIT_CHAIN_ADVISORY_LOCK);
                    lock.execute();
                }

                HashChainEngine chain = new HashChainEngine();
                try (PreparedStatement query = conn.prepareStatement(
                        "SELECT * FROM audit_entry ORDER BY sequence_number ASC");
                     ResultSet rs = query.executeQuery()) {
                    while (rs.next()) chain.restoreEntry(mapAuditEntry(rs));
                }
                if (chain.verifyChain().isPresent()) {
                    throw new IllegalStateException("Cannot append evidence to a broken audit chain");
                }

                AuditEntry entry = chain.append(chainPayload, AuditEntry.ActorRole.PU_OFFICER,
                        actorId, locationCode);
                sub.setChainEntryId(entry.getId());
                sub.setReceiptHash(entry.getCurrentHash());

                try (PreparedStatement ps = conn.prepareStatement(insertAudit)) {
                    ps.setObject(1, entry.getId());
                    ps.setLong(2, entry.getSequenceNumber());
                    ps.setString(3, entry.getPreviousHash());
                    ps.setString(4, entry.getPayload());
                    ps.setString(5, entry.getActorRole().name());
                    ps.setString(6, entry.getActorId());
                    ps.setString(7, entry.getTimestamp().toString());
                    ps.setString(8, entry.getCurrentHash());
                    ps.setString(9, entry.getSignature());
                    ps.setString(10, entry.getLocationCode());
                    ps.setString(11, entry.getStatus().name());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
                    ps.setObject(1, sub.getId());
                    ps.setString(2, sub.getIdempotencyKey());
                    ps.setString(3, sub.getActorId());
                    ps.setString(4, sub.getLocationCode());
                    ps.setString(5, sub.getEvidenceFingerprint());
                    ps.setString(6, sub.getContentType());
                    ps.setLong(7, sub.getSizeBytes());
                    ps.setString(8, sub.getPayloadJson());
                    ps.setString(9, sub.getSubmittedAt().toString());
                    ps.setObject(10, sub.getChainEntryId());
                    ps.setString(11, sub.getReceiptHash());
                    ps.setBytes(12, sub.getEvidenceBytes());
                    ps.executeUpdate();
                }
                conn.commit();
                return sub;
            } catch (Exception ex) {
                try { conn.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                if (ex instanceof RuntimeException runtime) throw runtime;
                throw new IllegalStateException("Atomic evidence submission failed", ex);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Atomic evidence submission failed", ex);
        }
    }

    private AuditEntry mapAuditEntry(ResultSet rs) throws SQLException {
        AuditEntry e = new AuditEntry();
        Object idObj = rs.getObject("id");
        e.setId(idObj instanceof UUID uuid ? uuid : UUID.fromString(idObj.toString()));
        e.setSequenceNumber(rs.getLong("sequence_number"));
        e.setPreviousHash(rs.getString("previous_hash"));
        e.setPayload(rs.getString("payload"));
        e.setActorRole(AuditEntry.ActorRole.valueOf(rs.getString("actor_role")));
        e.setActorId(rs.getString("actor_id"));
        String ts = rs.getString("timestamp");
        e.setTimestamp(ts == null ? Instant.now() : Instant.parse(ts));
        e.setCurrentHash(rs.getString("current_hash"));
        e.setSignature(rs.getString("signature"));
        e.setLocationCode(rs.getString("location_code"));
        e.setStatus(AuditEntry.EntryStatus.valueOf(rs.getString("status")));
        return e;
    }

    @Override public Optional<EvidenceSubmission> findByIdempotency(String key) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM evidence_submission WHERE idempotency_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                EvidenceSubmission e = new EvidenceSubmission();
                e.setId((UUID) rs.getObject("id"));
                e.setIdempotencyKey(rs.getString("idempotency_key"));
                e.setActorId(rs.getString("actor_id"));
                e.setLocationCode(rs.getString("location_code"));
                e.setEvidenceFingerprint(rs.getString("evidence_fingerprint"));
                e.setContentType(rs.getString("content_type"));
                e.setSizeBytes(rs.getLong("size_bytes"));
                e.setPayloadJson(rs.getString("payload_json"));
                e.setSubmittedAt(Instant.parse(rs.getString("submitted_at")));
                e.setChainEntryId((UUID) rs.getObject("chain_entry_id"));
                e.setReceiptHash(rs.getString("receipt_hash"));
                e.setEvidenceBytes(rs.getBytes("evidence_bytes"));
                return Optional.of(e);
            }
        } catch (SQLException ex) { throw new IllegalStateException("Failed to read evidence", ex); }
    }

    @Override public void save(EvidenceSubmission sub) {
        String sql = "INSERT INTO evidence_submission (id,idempotency_key,actor_id,location_code,evidence_fingerprint,content_type,size_bytes,payload_json,submitted_at,chain_entry_id,receipt_hash,evidence_bytes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, sub.getId());
            ps.setString(2, sub.getIdempotencyKey());
            ps.setString(3, sub.getActorId());
            ps.setString(4, sub.getLocationCode());
            ps.setString(5, sub.getEvidenceFingerprint());
            ps.setString(6, sub.getContentType());
            ps.setLong(7, sub.getSizeBytes());
            ps.setString(8, sub.getPayloadJson());
            ps.setString(9, sub.getSubmittedAt().toString());
            ps.setObject(10, sub.getChainEntryId());
            ps.setString(11, sub.getReceiptHash());
            ps.setBytes(12, sub.getEvidenceBytes());
            ps.executeUpdate();
        } catch (SQLException ex) { throw new IllegalStateException("Failed to save evidence", ex); }
    }

    @Override public java.util.List<java.util.Map<String, Object>> loadReviews() {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT id,evidence_id,vision_notes,arithmetic_consistent,requires_human_review,decision,reviewer_id,decided_at,reviewer_notes,enqueued_at FROM review_item ORDER BY enqueued_at";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("id", rs.getString("id")); row.put("evidenceId", rs.getString("evidence_id"));
                row.put("notes", rs.getString("vision_notes")); row.put("arithmeticConsistent", rs.getBoolean("arithmetic_consistent"));
                row.put("requiresHumanReview", rs.getBoolean("requires_human_review")); row.put("decision", rs.getString("decision"));
                row.put("reviewerId", rs.getString("reviewer_id")); row.put("decidedAt", rs.getString("decided_at"));
                row.put("reviewerNotes", rs.getString("reviewer_notes")); row.put("enqueuedAt", rs.getString("enqueued_at"));
                rows.add(row);
            }
            return rows;
        } catch (SQLException ex) { throw new IllegalStateException("Failed to load review queue", ex); }
    }

    @Override public void saveReview(String id, String evidenceId, String notes, boolean arithmeticOk, boolean needsReview, String decision, String enqueuedAt) {
        String sql = "INSERT INTO review_item (id,evidence_id,vision_notes,arithmetic_consistent,requires_human_review,decision,enqueued_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(id));
            ps.setString(2, evidenceId);
            ps.setString(3, notes);
            ps.setBoolean(4, arithmeticOk);
            ps.setBoolean(5, needsReview);
            ps.setString(6, decision);
            ps.setString(7, enqueuedAt);
            ps.executeUpdate();
        } catch (SQLException ex) { throw new IllegalStateException("Failed to save review", ex); }
    }

    @Override public void updateReviewDecision(String id, String decision, String reviewerId, String notes, String decidedAt) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE review_item SET decision=?, reviewer_id=?, reviewer_notes=?, decided_at=? WHERE id=?")) {
            ps.setString(1, decision);
            ps.setString(2, reviewerId);
            ps.setString(3, notes);
            ps.setString(4, decidedAt);
            ps.setObject(5, UUID.fromString(id));
            ps.executeUpdate();
        } catch (SQLException ex) { throw new IllegalStateException("Failed to update review", ex); }
    }

    @Override public void close() { /* Spring manages the DataSource. */ }
}

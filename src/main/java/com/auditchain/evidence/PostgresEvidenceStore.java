package com.auditchain.evidence;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** JDBC-backed evidence and review persistence using the existing Flyway V2 schema. */
public class PostgresEvidenceStore extends DurableEvidenceStore {
    private final DataSource dataSource;

    public PostgresEvidenceStore(DataSource dataSource) {
        super(false);
        this.dataSource = dataSource;
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
                return Optional.of(e);
            }
        } catch (SQLException ex) { throw new IllegalStateException("Failed to read evidence", ex); }
    }

    @Override public void save(EvidenceSubmission sub) {
        String sql = "INSERT INTO evidence_submission (id,idempotency_key,actor_id,location_code,evidence_fingerprint,content_type,size_bytes,payload_json,submitted_at,chain_entry_id,receipt_hash) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
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
            ps.executeUpdate();
        } catch (SQLException ex) { throw new IllegalStateException("Failed to save evidence", ex); }
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

package com.auditchain.persistence;

import com.auditchain.model.AuditEntry;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL-backed AuditStore for production / multi-instance use.
 * Relies on Flyway-managed schema. Uses transactions for append safety.
 * Sequence uniqueness is enforced by UNIQUE constraint on sequence_number.
 */
public class PostgresAuditStore implements AuditStore {

    private final DataSource dataSource;

    public PostgresAuditStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void append(AuditEntry entry) {
        String sql = """
            INSERT INTO audit_entry
            (id, sequence_number, previous_hash, payload, actor_role, actor_id,
             timestamp, current_hash, signature, location_code, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, entry.getId());
                ps.setLong(2, entry.getSequenceNumber());
                ps.setString(3, entry.getPreviousHash());
                ps.setString(4, entry.getPayload());
                ps.setString(5, entry.getActorRole().name());
                ps.setString(6, entry.getActorId());
                ps.setTimestamp(7, Timestamp.from(entry.getTimestamp()));
                ps.setString(8, entry.getCurrentHash());
                ps.setString(9, entry.getSignature());
                ps.setString(10, entry.getLocationCode());
                ps.setString(11, entry.getStatus().name());
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IllegalStateException("Failed to append entry " + entry.getSequenceNumber(), e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to append entry " + entry.getSequenceNumber(), e);
        }
    }

    @Override
    public List<AuditEntry> loadAll() {
        String sql = "SELECT * FROM audit_entry ORDER BY sequence_number ASC";
        List<AuditEntry> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load chain", e);
        }
        return list;
    }

    @Override
    public Optional<AuditEntry> getLatest() {
        String sql = "SELECT * FROM audit_entry ORDER BY sequence_number DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get latest entry", e);
        }
    }

    @Override
    public int size() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM audit_entry")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count entries", e);
        }
    }

    @Override
    public List<AuditEntry> findPendingSync() {
        String sql = "SELECT * FROM audit_entry WHERE status = ? ORDER BY sequence_number ASC";
        List<AuditEntry> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AuditEntry.EntryStatus.PENDING_SYNC.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query pending sync", e);
        }
        return list;
    }

    @Override
    public void markSynced(UUID entryId) {
        String sql = "UPDATE audit_entry SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AuditEntry.EntryStatus.SYNCED.name());
            ps.setObject(2, entryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark synced: " + entryId, e);
        }
    }

    @Override
    public void close() {
        // DataSource is managed by Spring; nothing to close here.
    }

    private AuditEntry mapRow(ResultSet rs) throws SQLException {
        AuditEntry e = new AuditEntry();
        Object idObj = rs.getObject("id");
        if (idObj instanceof UUID) {
            e.setId((UUID) idObj);
        } else {
            e.setId(UUID.fromString(idObj.toString()));
        }
        e.setSequenceNumber(rs.getLong("sequence_number"));
        e.setPreviousHash(rs.getString("previous_hash"));
        e.setPayload(rs.getString("payload"));
        e.setActorRole(AuditEntry.ActorRole.valueOf(rs.getString("actor_role")));
        e.setActorId(rs.getString("actor_id"));
        Timestamp ts = rs.getTimestamp("timestamp");
        e.setTimestamp(ts != null ? ts.toInstant() : Instant.now());
        e.setCurrentHash(rs.getString("current_hash"));
        e.setSignature(rs.getString("signature"));
        e.setLocationCode(rs.getString("location_code"));
        e.setStatus(AuditEntry.EntryStatus.valueOf(rs.getString("status")));
        return e;
    }
}

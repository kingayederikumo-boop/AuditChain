package com.auditchain.persistence;

import com.auditchain.model.AuditEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite-backed store for offline-first operation.
 * File lives under ./data/auditchain.db by default.
 */
public class SqliteAuditStore implements AuditStore {

    private final Connection connection;

    public SqliteAuditStore(Path dbPath) {
        try {
            String url;
            if (dbPath == null) {
                url = "jdbc:sqlite::memory:";
            } else {
                Path parent = dbPath.toAbsolutePath().getParent();
                if (parent != null) Files.createDirectories(parent);
                url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
            }
            this.connection = DriverManager.getConnection(url);
            this.connection.setAutoCommit(true);
            initSchema();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open SQLite store", e);
        }
    }

    /** File-backed store under ./data/auditchain.db */
    public SqliteAuditStore() {
        this(Path.of("data", "auditchain.db"));
    }

    /** Pure in-memory SQLite (tests / restricted environments) */
    public static SqliteAuditStore inMemory() {
        return new SqliteAuditStore(null);
    }

    private void initSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS audit_entry (
                id              TEXT PRIMARY KEY,
                sequence_number INTEGER NOT NULL UNIQUE,
                previous_hash   TEXT NOT NULL,
                payload         TEXT NOT NULL,
                actor_role      TEXT NOT NULL,
                actor_id        TEXT NOT NULL,
                timestamp       TEXT NOT NULL,
                current_hash    TEXT NOT NULL,
                signature       TEXT,
                location_code   TEXT,
                status          TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_status ON audit_entry(status);
            CREATE INDEX IF NOT EXISTS idx_seq    ON audit_entry(sequence_number);
            """;
        try (Statement st = connection.createStatement()) {
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) st.executeUpdate(statement);
            }
        }
    }

    @Override
    public void append(AuditEntry entry) {
        String sql = """
            INSERT INTO audit_entry
            (id, sequence_number, previous_hash, payload, actor_role, actor_id,
             timestamp, current_hash, signature, location_code, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entry.getId().toString());
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
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to append entry " + entry.getSequenceNumber(), e);
        }
    }

    @Override
    public List<AuditEntry> loadAll() {
        String sql = "SELECT * FROM audit_entry ORDER BY sequence_number ASC";
        List<AuditEntry> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
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
        try (Statement st = connection.createStatement();
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
        try (Statement st = connection.createStatement();
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, AuditEntry.EntryStatus.SYNCED.name());
            ps.setString(2, entryId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark synced: " + entryId, e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // best-effort
        }
    }

    private AuditEntry mapRow(ResultSet rs) throws SQLException {
        AuditEntry e = new AuditEntry();
        e.setId(UUID.fromString(rs.getString("id")));
        e.setSequenceNumber(rs.getLong("sequence_number"));
        e.setPreviousHash(rs.getString("previous_hash"));
        e.setPayload(rs.getString("payload"));
        e.setActorRole(AuditEntry.ActorRole.valueOf(rs.getString("actor_role")));
        e.setActorId(rs.getString("actor_id"));
        e.setTimestamp(Instant.parse(rs.getString("timestamp")));
        e.setCurrentHash(rs.getString("current_hash"));
        e.setSignature(rs.getString("signature"));
        e.setLocationCode(rs.getString("location_code"));
        e.setStatus(AuditEntry.EntryStatus.valueOf(rs.getString("status")));
        return e;
    }
}

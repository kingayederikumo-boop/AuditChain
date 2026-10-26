-- AuditChain core table. Compatible with both SQLite (manual) and PostgreSQL (Flyway).
CREATE TABLE IF NOT EXISTS audit_entry (
    id              UUID PRIMARY KEY,
    sequence_number BIGINT NOT NULL UNIQUE,
    previous_hash   TEXT NOT NULL,
    payload         TEXT NOT NULL,
    actor_role      TEXT NOT NULL,
    actor_id        TEXT NOT NULL,
    timestamp       TIMESTAMPTZ NOT NULL,
    current_hash    TEXT NOT NULL,
    signature       TEXT,
    location_code   TEXT,
    status          TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_entry_status ON audit_entry(status);
CREATE INDEX IF NOT EXISTS idx_audit_entry_seq    ON audit_entry(sequence_number);

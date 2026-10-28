-- PostgreSQL evidence and review persistence schema.
-- Idempotent so it can repair deployments whose schema was only partially initialized.
CREATE TABLE IF NOT EXISTS evidence_submission (
    id UUID PRIMARY KEY,
    idempotency_key TEXT NOT NULL UNIQUE,
    actor_id TEXT NOT NULL,
    location_code TEXT,
    evidence_fingerprint TEXT NOT NULL,
    content_type TEXT,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    payload_json TEXT NOT NULL,
    submitted_at TEXT NOT NULL,
    chain_entry_id UUID,
    receipt_hash TEXT,
    evidence_bytes BYTEA
);

CREATE INDEX IF NOT EXISTS idx_evidence_submission_actor ON evidence_submission(actor_id);
CREATE INDEX IF NOT EXISTS idx_evidence_submission_chain_entry ON evidence_submission(chain_entry_id);

CREATE TABLE IF NOT EXISTS review_item (
    id UUID PRIMARY KEY,
    evidence_id TEXT NOT NULL,
    vision_notes TEXT,
    arithmetic_consistent BOOLEAN NOT NULL DEFAULT FALSE,
    requires_human_review BOOLEAN NOT NULL DEFAULT TRUE,
    decision TEXT NOT NULL DEFAULT 'PENDING',
    reviewer_id TEXT,
    decided_at TEXT,
    reviewer_notes TEXT,
    enqueued_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_review_item_enqueued_at ON review_item(enqueued_at);

ALTER TABLE evidence_submission ENABLE ROW LEVEL SECURITY;
ALTER TABLE review_item ENABLE ROW LEVEL SECURITY;

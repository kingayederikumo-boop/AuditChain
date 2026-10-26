-- Evidence submissions (idempotency + fingerprints) and human-review items
CREATE TABLE IF NOT EXISTS evidence_submission (
    id                  UUID PRIMARY KEY,
    idempotency_key     TEXT NOT NULL UNIQUE,
    actor_id            TEXT NOT NULL,
    location_code       TEXT,
    evidence_fingerprint TEXT NOT NULL,
    content_type        TEXT,
    size_bytes          BIGINT NOT NULL DEFAULT 0,
    payload_json        TEXT,
    submitted_at        TEXT NOT NULL,
    chain_entry_id      UUID,
    receipt_hash        TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_evidence_idempotency ON evidence_submission(idempotency_key);

CREATE TABLE IF NOT EXISTS review_item (
    id                  UUID PRIMARY KEY,
    evidence_id         TEXT NOT NULL,
    vision_notes        TEXT,
    arithmetic_consistent BOOLEAN NOT NULL DEFAULT FALSE,
    requires_human_review BOOLEAN NOT NULL DEFAULT TRUE,
    decision            TEXT NOT NULL DEFAULT 'PENDING',
    reviewer_id         TEXT,
    decided_at          TEXT,
    reviewer_notes      TEXT,
    enqueued_at         TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_review_pending ON review_item(decision);

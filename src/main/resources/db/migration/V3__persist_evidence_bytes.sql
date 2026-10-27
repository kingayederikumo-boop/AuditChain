-- Persist original uploaded image bytes with evidence receipts.
ALTER TABLE evidence_submission ADD COLUMN IF NOT EXISTS evidence_bytes BYTEA;

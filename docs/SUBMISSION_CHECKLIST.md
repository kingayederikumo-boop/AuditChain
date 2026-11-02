# Submission checklist

**Project:** AuditChain — #AI4Elections 2026, Track 4 + Climate Extra Track  
**Last reviewed:** 2026-11-02

## Core prototype and evidence

- [x] Hash-chained audit trail with verification.
- [x] SQLite default storage and PostgreSQL support in code.
- [x] Evidence submission with SHA-256 fingerprint, idempotency, and receipt; on PostgreSQL the receipt and chain entry commit in one transaction.
- [x] Mock AI path and optional HTTP provider; every first-time AI result is recorded on the chain; human review queue; AI does not autonomously declare fraud.
- [x] Ed25519 chain-tip checkpoints and historical checkpoint regression tests.
- [x] Verification of the live chain against the public anchor log (`GET /api/checkpoints/verify`), with a test that a consistently re-hashed history, which passes internal verification, fails the anchor check.
- [x] Role-based API keys (OFFICER / REVIEWER / AUDITOR / ADMIN); actor IDs are HMAC pseudonyms.
- [x] Process-local rate limiting, documented as a prototype limitation.
- [x] CI workflow running `mvn verify`.
- [x] Docker Compose for local PostgreSQL.
- [x] `.env.example` without production secrets in git.

## Submission documentation

- [x] [Privacy notice](PRIVACY.md).
- [x] [Responsible AI statement](RESPONSIBLE_AI.md).
- [x] [Accessibility statement and test plan](ACCESSIBILITY.md) — requirements documented; execution still pending.
- [x] [Evaluation plan and results status](EVALUATION.md).
- [x] [Deployment guide](DEPLOYMENT.md).
- [x] [Pre-existing work and attribution disclosure](PRE_EXISTING_WORK.md) — owner has stated that no earlier AuditChain work or reused prior-project work is included and that this is a solo project.
- [x] [STRIDE threat model](STRIDE_Threat_Model.md).
- [x] [Checkpoint anchoring setup](ANCHORING.md).
- [x] Root [MIT License](../LICENSE).

## Remaining before a fully verified submission

- [x] Project owner supplied the pre-existing-work and contributor-history confirmation; retain the disclosure in the submission.
- [ ] Execute and record the accessibility tests in ACCESSIBILITY.md.
- [ ] Run CI on the exact final submission commit and record the result. Re-confirm after any later commit.
- [ ] Run HTTP smoke tests against the live health, authentication, evidence submission/receipt, review, and checkpoint-verification endpoints.
- [x] Existing AuditChain Supabase PostgreSQL backend is connected to Render; deployment logs of 2026-11-02 confirmed the PostgreSQL store and five validated Flyway migrations (schema version 4, up to date). No new database was created.
- [ ] Verify durable writes by submitting synthetic test data, confirming it remains after an application restart, and checking audit-chain/checkpoint recovery. Do not use real electoral data.
- [x] Scheduled GitHub checkpoint anchoring is configured and operational. The public `audit-anchors/anchors/log.jsonl` held nine records (sequences 2 to 10) on 2026-11-01 and gains one record per run. This is same-repository publication, not an independent trust domain.
- [x] Documentation states that the mock AI path is the default and a real vision provider is optional; owner should verify this matches the intended submission configuration.
- [ ] Check dependency licenses and confirm that no third-party assets or datasets are missing attribution.

## Honesty rule

Do not mark production persistence, accessibility conformance, independent anchoring, real-model performance, or production readiness as complete until the corresponding verification evidence exists. Use synthetic data only; never connect this prototype to live electoral infrastructure.

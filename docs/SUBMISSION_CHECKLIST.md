# Submission checklist

**Project:** AuditChain — #AI4Elections 2026, Track 4  
**Last reviewed:** 2026-10-30

## Core prototype and evidence

- [x] Hash-chained audit trail with verification.
- [x] SQLite default storage and PostgreSQL support in code.
- [x] Evidence submission with SHA-256 fingerprint, idempotency, and receipt.
- [x] Mock AI path and optional HTTP provider; human review queue; AI does not autonomously declare fraud.
- [x] Ed25519 chain-tip checkpoints and historical checkpoint regression tests.
- [x] Role-based API keys (OFFICER / REVIEWER / AUDITOR / ADMIN).
- [x] Process-local rate limiting, documented as a prototype limitation.
- [x] CI workflow running `mvn verify`.
- [x] Docker Compose for local PostgreSQL.
- [x] `.env.example` without production secrets in git.

## Submission documentation

- [x] [Privacy notice](PRIVACY.md) — describes synthetic-only scope and current storage/provider limitations.
- [x] [Responsible AI statement](RESPONSIBLE_AI.md) — human oversight, prohibited uses, and residual risks.
- [x] [Accessibility statement and test plan](ACCESSIBILITY.md) — requirements and verification steps; audit not yet completed.
- [x] [Evaluation plan and results status](EVALUATION.md) — reproducible methods and evidence currently available.
- [x] [Deployment guide](DEPLOYMENT.md) — local/container deployment, secret handling, external PostgreSQL, and verification.
- [x] [Pre-existing work and attribution disclosure](PRE_EXISTING_WORK.md) — disclosure draft with owner-confirmation checklist.
- [x] [STRIDE threat model](STRIDE_Threat_Model.md).
- [x] [Checkpoint anchoring setup](ANCHORING.md).
- [x] Root [MIT License](../LICENSE).

## Remaining before a fully verified submission

- [ ] Project owner confirms the pre-existing-work disclosure, including any work created outside this repository before the project start.
- [ ] Execute and record the accessibility tests.
- [ ] Run CI on the exact final submission commit and record the result (this checklist update will trigger a new CI run).
- [ ] Run HTTP smoke tests against the live health, authentication, evidence submission/receipt, review, and checkpoint-verification endpoints. Render logs confirm the app starts, but this tool session could not independently fetch the live endpoints.
- [x] Existing AuditChain Supabase PostgreSQL backend is connected to Render; application logs confirm a PostgreSQL connection and five validated migrations (schema version 4, up to date). No new database was created.
- [ ] Verify durable writes by submitting synthetic test data, confirming it remains after an application restart, and checking audit-chain/checkpoint recovery. Do not use real electoral data.
- [ ] Configure GitHub repository variable `AUDITCHAIN_BASE_URL` and secret `AUDITCHAIN_AUDITOR_KEY` for scheduled checkpoint anchoring; the GitHub connector cannot inspect or manage repository secrets.
- [x] Documentation states that the mock AI path is the default and a real vision provider is optional; owner should verify this matches the intended submission configuration.
- [ ] Check dependency licenses and confirm that no third-party assets or datasets are missing attribution.

## Honesty rule

Do not mark production persistence, accessibility conformance, independent anchoring, real-model performance, or production readiness as complete until the corresponding verification evidence exists. Use synthetic data only; never connect this prototype to live electoral infrastructure.

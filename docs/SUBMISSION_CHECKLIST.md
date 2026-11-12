# Submission checklist

**Project:** AuditChain, #AI4Elections 2026, Track 4 + Climate Extra Track  
**Last reviewed:** 2026-11-12

## Core prototype and evidence

- [x] Hash-chained audit trail with verification.
- [x] SQLite default storage and PostgreSQL support in code.
- [x] Evidence submission with SHA-256 fingerprint, idempotency and receipt; on PostgreSQL the receipt and chain entry commit in one transaction.
- [x] Mock AI path and optional HTTP provider; every first-time AI result is recorded on the chain; human review queue; AI does not autonomously declare fraud.
- [x] Ed25519 chain-tip checkpoints and historical checkpoint regression tests.
- [x] Verification of the live chain against the public anchor log (`GET /api/checkpoints/verify`), with a test that a consistently re-hashed history fails the anchor check.
- [x] Role-based API keys; actor IDs are HMAC pseudonyms; destructive demo routes are ADMIN-only (tested).
- [x] Rate limits on submission and on public verification; paginated chain endpoint.
- [x] CI workflow running `mvn verify` (44+ tests).
- [x] Reproducible evaluation harness with measured results (EVALUATION.md).
- [x] Language selector: English, Naijá Pidgin, Yorùbá, Igbo, Hausa (LANGUAGES.md).
- [x] Automated accessibility scan, clean after fixes (ACCESSIBILITY.md).
- [x] Keep-alive workflow, manual load-test workflow, Internet Archive snapshot of the anchor log.
- [x] Docker Compose for local PostgreSQL; `.env.example` without secrets.

## Submission documentation

- [x] [Privacy notice](PRIVACY.md)
- [x] [Responsible AI statement](RESPONSIBLE_AI.md)
- [x] [Accessibility statement and results](ACCESSIBILITY.md)
- [x] [Evaluation plan and measured results](EVALUATION.md)
- [x] [Languages](LANGUAGES.md)
- [x] [Pathway to real-world use](PATHWAY.md)
- [x] [Deployment guide](DEPLOYMENT.md) and [storage status](STORAGE_STATUS.md)
- [x] [Pre-existing work disclosure](PRE_EXISTING_WORK.md): the owner states there is no earlier AuditChain work or reused prior-project work, it was created for this hackathon, and it has not won an award or grant elsewhere.
- [x] [STRIDE threat model](STRIDE_Threat_Model.md), [anchoring setup](ANCHORING.md), [tamper demo](TAMPER_DEMO.md)
- [x] [2-minute demo script](DEMO_SCRIPT.md)
- [x] Root [MIT License](../LICENSE)

## Remaining before a fully verified submission

- [ ] Record the load-test result in EVALUATION.md.
- [ ] Manual accessibility tests (screen reader, live keyboard run, zoom, real phone).
- [ ] Native-speaker review of the Pidgin, Yoruba, Igbo and Hausa text; Ijaw translator.
- [ ] Direct restart-persistence test on the live service.
- [ ] Live HTTP smoke test: health, authentication, submission and receipt, review, checkpoint verification.
- [ ] Blank or rotate the temporary admin API key.
- [ ] Independent anchor mirror under a separately administered account, if wanted.
- [ ] Record and upload the 2-minute video.
- [ ] Check dependency licences and attribution.
- [ ] Run CI on the exact final submission commit and record the result.

## Honesty rule

Do not mark production persistence, accessibility conformance, independent anchoring, real-model performance or production readiness as complete until the verification evidence exists. Use synthetic data only; never connect this prototype to live electoral infrastructure.

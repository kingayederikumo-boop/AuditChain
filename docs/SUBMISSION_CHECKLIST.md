# Submission checklist (prototype)

- [x] Hash-chained audit trail with verification
- [x] SQLite offline-first persistence + Postgres option
- [x] Evidence submission with SHA-256 fingerprint + durable idempotency
- [x] AI path: mock + optional HTTP provider; human review queue; no autonomous fraud claim
- [x] Ed25519 checkpoints of chain tip
- [x] Role API keys (OFFICER / REVIEWER / AUDITOR / ADMIN)
- [x] Rate limiting (process-local)
- [x] CI on every push (`mvn verify`)
- [x] docker-compose for local Postgres
- [x] `.env.example` — no secrets in git
- [ ] Real vision model API key (user-supplied)
- [ ] Production Postgres URL / managed disk (user-supplied)
- [ ] Public demo host with persistent volume (user deploy)

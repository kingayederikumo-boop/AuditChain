# AuditChain

Tamper-evident, offline-tolerant audit trail for **simulated** electoral results transmission.  
Java 17 / Spring Boot 3. Nigerian #AI4Elections prototype (Track 4, with the climate-resilience area).

Live: https://auditchain.onrender.com (free hosting; the first request after idle can take about 80 seconds).

## Quick start

```bash
mvn -B clean verify
java -jar target/auditchain-0.1.0-SNAPSHOT.jar
# GET /health  GET /api/chain  GET /api/checkpoints/verify
```

## Features

| Area | Capability |
|------|------------|
| Hash chain | SHA-256 linked entries, verified on load, append to a broken chain is rejected |
| Storage | SQLite file-backed (default) or PostgreSQL + Flyway |
| Evidence | `POST /api/evidence`: SHA-256 fingerprint, durable idempotency, receipt |
| Screening | Arithmetic check of declared totals against party figures; flagged sheets go to a human review queue (the checker never declares fraud) |
| Crypto | Ed25519 signed checkpoints of the chain tip, published to a public anchor log and archived with the Internet Archive |
| Public verification | `GET /api/checkpoints/verify` compares the live chain with the published signed checkpoints, no login needed |
| Security | Role API keys (OFFICER / REVIEWER / AUDITOR / ADMIN), default-deny for other mutations, rate limits |
| Inclusion | Language selector: English, Naijá Pidgin, Yorùbá, Igbo, Hausa; offline submission queue; automated accessibility scan |
| Resilience | Climate/connectivity disruption simulation with a signed checkpoint and optional Telegram alert |

## Measured results

From the seeded evaluation harness (see [EVALUATION.md](docs/EVALUATION.md)): 0 of 100 clean sheets falsely flagged; 60 of 60 altered totals flagged; 100 of 100 naive record edits detected by the chain; consistent rewrites are missed by the chain alone (0 of 100) and caught against the signed anchored tip (100 of 100). These are synthetic-data results, not field performance.

## Auth (environment variables; never commit secrets)

```bash
export AUDITCHAIN_API_KEY_OFFICER=off-secret
export AUDITCHAIN_API_KEY_REVIEWER=rev-secret
export AUDITCHAIN_API_KEY_AUDITOR=aud-secret
# or single admin key:
export AUDITCHAIN_API_KEY=admin-secret
```

Send the key in the `X-API-Key` header. Auditor or admin access is required to create checkpoints and run the climate-disruption simulation. Demo-only routes (`/api/reset`, `/api/tamper`, `/api/generate`) are admin-only.

## Signed checkpoints

- `GET /api/checkpoints/public-key` returns the Ed25519 public key.
- `POST /api/checkpoints` signs the current chain tip (auditor/admin).
- `GET /api/checkpoints` lists checkpoints created during the current process.
- Set `AUDITCHAIN_ED25519_SEED` to a stable 32-byte seed encoded as 64 hexadecimal characters. Keep it private and stable across restarts.

## Telegram notifications

Optional and non-blocking. Configure `AUDITCHAIN_TELEGRAM_BOT_TOKEN` and `AUDITCHAIN_TELEGRAM_CHAT_ID` on the server only.

## Local tamper demonstration

For local synthetic data only. Keep `AUDITCHAIN_DEMO_ENABLED=false` in every deployed environment. Follow [the isolated tamper demo](docs/TAMPER_DEMO.md).

## Climate-disruption simulation

`POST /api/scenarios/climate-disruption` accepts `outageMinutes` (default 120; 1-10080), `affectedUnits` (default 12; 1-10000) and `queuedSubmissions` (default 240; 0-1000000). The response is marked `simulationOnly: true`; it signs a checkpoint and reports a simulated recovery. It does not create, modify or delete evidence records.

## Local Postgres (Docker)

```bash
docker compose up -d
export AUDITCHAIN_STORE_TYPE=postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auditchain
export SPRING_DATASOURCE_USERNAME=auditchain
export SPRING_DATASOURCE_PASSWORD=auditchain
java -jar target/auditchain-*.jar --spring.profiles.active=postgres
```

## Important limits

- SQLite on ephemeral host disks is **not** production persistence.
- The default checker is deterministic and arithmetic-only; it does not read images. Set `AUDITCHAIN_VISION_URL` for an HTTP provider (not evaluated).
- Rate limiters are process-local.
- A passing integrity check shows recorded history was not altered; it does not show the original figures were true.
- The anchor log lives in this repository; the Internet Archive snapshots add an outside copy, and a separately administered mirror is supported but not configured.
- Synthetic data only. No real election systems.

## CI and tools

`mvn -B verify` runs on every push to `main`. Workflows also publish signed checkpoints (every 6 hours), ping `/health` (every 10 minutes), and run a manual load test. `scripts/a11y-check.js` runs the accessibility scan; `scripts/verify-tamper-demo.py` checks the tamper demo.

## Documents

- [Privacy notice](docs/PRIVACY.md)
- [Responsible AI statement](docs/RESPONSIBLE_AI.md)
- [Accessibility statement and results](docs/ACCESSIBILITY.md)
- [Languages](docs/LANGUAGES.md)
- [Evaluation and measured results](docs/EVALUATION.md)
- [Pathway to real-world use](docs/PATHWAY.md)
- [Deployment guide](docs/DEPLOYMENT.md) and [storage status](docs/STORAGE_STATUS.md)
- [Pre-existing work and attribution disclosure](docs/PRE_EXISTING_WORK.md)
- [Threat model](docs/STRIDE_Threat_Model.md)
- [Checkpoint anchoring setup](docs/ANCHORING.md)
- [Submission checklist](docs/SUBMISSION_CHECKLIST.md)
- [2-minute demo script](docs/DEMO_SCRIPT.md)

## License

AuditChain's project contributions are licensed under the [MIT License](LICENSE). Third-party dependencies remain subject to their own licenses.

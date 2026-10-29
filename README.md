# AuditChain

Tamper-evident, offline-tolerant audit trail for **simulated** electoral results transmission.  
Java 17 / Spring Boot 3 — Nigerian #AI4Elections prototype.

## Quick start

```bash
mvn -B clean verify
java -jar target/auditchain-0.1.0-SNAPSHOT.jar
# GET /health  GET /api/chain
```

## Features

| Area | Capability |
|------|------------|
| Hash chain | SHA-256 linked entries, verify on load, reject append to broken chain |
| Storage | SQLite file-backed (default) or PostgreSQL + Flyway |
| Evidence | `POST /api/evidence` — SHA-256 fingerprint, durable idempotency, receipt |
| AI | Mock vision + optional HTTP provider; human review queue (AI never declares fraud) |
| Crypto | Ed25519 checkpoints of chain tip |
| Resilience | Climate/connectivity disruption simulation with signed checkpoint and optional Telegram alert |
| Security | Role API keys (OFFICER / REVIEWER / AUDITOR / ADMIN), rate limit |

## Auth (environment variables — never commit secrets)

```bash
export AUDITCHAIN_API_KEY_OFFICER=off-secret
export AUDITCHAIN_API_KEY_REVIEWER=rev-secret
export AUDITCHAIN_API_KEY_AUDITOR=aud-secret
# or single admin key:
export AUDITCHAIN_API_KEY=admin-secret
```

Send the key in the `X-API-Key` header. Auditor or admin access is required to create checkpoints and run the climate-disruption simulation.

## Signed checkpoints

- `GET /api/checkpoints/public-key` returns the Ed25519 public key.
- `POST /api/checkpoints` signs the current chain tip (auditor/admin).
- `GET /api/checkpoints` lists checkpoints created during the current process.
- Set `AUDITCHAIN_ED25519_SEED` to a stable 32-byte seed encoded as 64 hexadecimal characters. Keep it private and stable across restarts; if unset, a random key is generated and checkpoints cannot be verified against the same identity after restart.

## Telegram notifications

Telegram delivery is optional and does not block the audit workflow. Configure both environment variables on the server:

- `AUDITCHAIN_TELEGRAM_BOT_TOKEN`
- `AUDITCHAIN_TELEGRAM_CHAT_ID`

The bot token must remain server-side and must never be included in the app or source repository. When credentials are missing or Telegram is unreachable, the API reports that no notification was sent.

## Climate-disruption simulation

`POST /api/scenarios/climate-disruption` accepts these optional query parameters:

- `outageMinutes` (default 120; allowed 1–10080)
- `affectedUnits` (default 12; allowed 1–10000)
- `queuedSubmissions` (default 240; allowed 0–1000000)

The response is explicitly marked `simulationOnly: true`. It creates a signed checkpoint of the current chain tip, reports a simulated reconnection/reconciliation outcome, and attempts an optional Telegram alert. It does not create, modify, or delete evidence records. This is a demonstration scenario, not a claim that a real outage or recovery occurred.

## Local Postgres (Docker)

```bash
docker compose up -d
export AUDITCHAIN_STORE_TYPE=postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auditchain
export SPRING_DATASOURCE_USERNAME=auditchain
export SPRING_DATASOURCE_PASSWORD=auditchain
java -jar target/auditchain-*.jar --spring.profiles.active=postgres
```

## Postgres

```bash
export AUDITCHAIN_STORE_TYPE=postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auditchain
export SPRING_DATASOURCE_USERNAME=auditchain
export SPRING_DATASOURCE_PASSWORD=...
java -jar target/auditchain-*.jar --spring.profiles.active=postgres
```

## Important limits

- SQLite on ephemeral host disks is **not** production persistence.
- Vision defaults to **mock**; set `AUDITCHAIN_VISION_URL` for an HTTP provider.
- Rate limiter is process-local.
- Checkpoints are held in memory by this prototype; export/publish them externally if they must survive a restart.
- Synthetic data only — no real election systems.

## CI

`mvn -B verify` on every push to `main`.

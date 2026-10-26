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
| AI | Mock vision + arithmetic check; human review queue (AI never declares fraud) |
| Crypto | Ed25519 checkpoints of chain tip |
| Security | Role API keys (OFFICER / REVIEWER / AUDITOR / ADMIN), rate limit |

## Auth (env vars — no secrets in git)

```bash
export AUDITCHAIN_API_KEY_OFFICER=off-secret
export AUDITCHAIN_API_KEY_REVIEWER=rev-secret
export AUDITCHAIN_API_KEY_AUDITOR=aud-secret
# or single admin key:
export AUDITCHAIN_API_KEY=admin-secret
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
- Vision is a **mock**; plug a real provider behind `VisionProvider` when you have keys.
- Rate limiter is process-local.
- Synthetic data only — no real election systems.

## CI

`mvn -B verify` on every push to `main`.

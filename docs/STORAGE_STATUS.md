# Storage status

| Path | Backend | Survives restart | Notes |
|------|---------|------------------|-------|
| Audit chain | SQLite `data/auditchain.db` (default) | Yes (on persistent disk) | Ephemeral hosts lose data |
| Audit chain | PostgreSQL + Flyway | Yes | `AUDITCHAIN_STORE_TYPE=postgres` + compose or cloud URL |
| Evidence + idempotency | SQLite `data/evidence.db` | Yes | Separate file; same caveats as above |
| Review decisions | Same evidence DB | Yes | Pending queue also kept in-memory for live list |
| Rate limiter | In-process map | No | Prototype only |
| Checkpoints | In-process list + Ed25519 | No across restarts | Publish externally if needed |

Local Postgres: `docker compose up -d` then profile `postgres`.

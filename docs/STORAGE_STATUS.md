# Storage status

**Last reviewed:** 2026-11-11

| Data | Backend | Current status | Durability boundary |
|------|---------|----------------|---------------------|
| Audit chain | Supabase PostgreSQL via Render `postgres` profile | Connection and Flyway migrations (version 4) confirmed in deployment logs | Chain sequence numbers continued across repeated restarts (indirect evidence); a direct restart test is still to be recorded |
| Evidence and idempotency | PostgreSQL when `AUDITCHAIN_STORE_TYPE=postgres` is active | Uses the configured external database | As above |
| Review decisions | PostgreSQL-backed persistence with pending queue also held in memory | Persisted decisions and in-memory pending-list behavior are distinct | Do not treat the live queue as durable |
| Rate limiters (submission and `/verify`) | In-process maps | Prototype limitation | Reset on process restart; not distributed |
| Active checkpoint list | In-process application state, rebuilt from the chain at startup | Checkpoints are also stored as chain events and published by the anchor workflow | The in-memory list itself is not durable |
| Published signed anchors | Public `audit-anchors` Git branch, `anchors/log.jsonl` | 31 records, all signatures verified offline on 2026-11-10 | Same-repository branch is not an independent trust domain |

## Database configuration

The existing Supabase project named **AuditChain** is connected to the Render service. Do not create a replacement database or reset the existing schema.

The configured PostgreSQL profile uses `AUDITCHAIN_STORE_TYPE=postgres` and `SPRING_PROFILES_ACTIVE=postgres`. Connection credentials must remain in Render secrets/environment settings and must never be committed.

## Remaining verification

Submit synthetic evidence, confirm its receipt and chain entry, restart/redeploy the service, then verify that the same evidence, receipt, chain state, and review decisions remain available. Until this test is recorded, describe persistence as indirectly evidenced, not independently proven.

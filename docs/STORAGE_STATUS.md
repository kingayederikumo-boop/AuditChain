# Storage status

**Last reviewed:** 2026-11-01

| Data | Backend | Current status | Durability boundary |
|------|---------|----------------|---------------------|
| Audit chain | Existing AuditChain Supabase PostgreSQL via Render `postgres` profile | Connection and five Flyway migrations were confirmed in deployment logs | Restart-survival of a synthetic record still needs a direct test |
| Evidence and idempotency | PostgreSQL when `AUDITCHAIN_STORE_TYPE=postgres` is active | Uses the configured external database | Restart-survival still needs a direct test |
| Review decisions | PostgreSQL-backed persistence with pending queue also held in memory | Persisted decisions and in-memory pending-list behavior are distinct | Verify after restart; do not treat the live queue as durable |
| Rate limiter | In-process map | Prototype limitation | Resets on process restart; not distributed |
| Active checkpoint list | In-process application state | Reconstructed/published checkpoint records are available through the anchor workflow | The in-memory list itself is not durable across restarts |
| Published signed anchors | Public `audit-anchors` Git branch, `anchors/log.jsonl` | Ten records marked valid were present on 2026-11-01; scheduled workflow run #14 succeeded | Same-repository branch is not an independent trust domain |

## Database configuration

The existing Supabase project named **AuditChain** is connected to the Render service. Prior deployment logs confirmed PostgreSQL connectivity and five validated Flyway migrations (schema version 4, up to date). Do not create a replacement database or reset the existing schema.

The configured PostgreSQL profile uses `AUDITCHAIN_STORE_TYPE=postgres` and `SPRING_PROFILES_ACTIVE=postgres`. Connection credentials must remain in Render secrets/environment settings and must never be committed.

## Remaining verification

Submit synthetic evidence, confirm its receipt and chain entry, restart/redeploy the service, then verify that the same evidence, receipt, chain state, and review decisions remain available. Until this test is recorded, describe the database connection as configured and migration-validated, but do not claim restart persistence has been independently proven.

The rate limiter and active in-memory checkpoint list remain process-local prototype limitations.

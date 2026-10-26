# Storage and deployment status

The application currently uses SQLite at AUDITCHAIN_SQLITE_PATH (default data/auditchain.db).
It retains entries across application restarts only when that path is on a persistent filesystem.
Render free service filesystems are ephemeral; this SQLite setup must NOT be described as durable on Render free.
Production requires PostgreSQL or another managed durable database before accepting submissions.

Demo mutation endpoints are disabled unless AUDITCHAIN_DEMO_ENABLED=true.
The /api/tamper demo mutates an in-memory entry only and is not a persisted attack.
Do not expose demo mode on a publicly accessible deployment.

GitHub Actions is configured to run Maven verify. A committed test is not evidence that CI passed.
Do not claim multi-instance append safety, independent anchoring, or production readiness from this stage.

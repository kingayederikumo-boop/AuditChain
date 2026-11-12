# Deployment Guide

**Project:** AuditChain  
**Status:** Prototype deployment guide  
**Last reviewed:** 2026-11-12

## Current deployment posture

- Application: Java 17 / Spring Boot 3, containerized with the repository Dockerfile.
- Hosting: Render web service `auditchain`, free plan, auto-deploy from `main`.
- Cold start: the free instance took about 74 to 83 seconds to start in the Render logs. It sleeps after about 15 minutes idle. The `Keep alive` workflow pings `/health` every 10 minutes to reduce this; warm the service before any demonstration regardless.
- Database: the existing Supabase project named **AuditChain**, connected over PostgreSQL. Deployment logs confirmed the PostgreSQL store and Flyway migrations up to version 4. No new database should be created.
- Persistence: chain sequence numbers continued across repeated service restarts, which is indirect evidence of persistence. A direct synthetic write, restart and read test is still to be recorded.
- Scheduled signed-checkpoint anchoring runs every six hours and appends to the public `audit-anchors` branch. On 2026-11-10 the log held 31 records, and all 31 Ed25519 signatures verified offline under one public key. Each run also asks the Internet Archive to snapshot the log (best effort). The branch is in the same repository, so it is not by itself an independent trust domain.
- A temporary admin API key is currently set on the service for testing. Blank or rotate it before the final submission.

## Local run

Requirements: Java 17+ and Maven.

```bash
mvn -B clean verify
mvn -B package
java -jar target/auditchain-0.1.0-SNAPSHOT.jar
```

Check `GET /health` and the chain verification endpoint. Use only synthetic records.

## Container run

```bash
docker build -t auditchain .
docker run --rm -p 8080:8080 --env-file .env auditchain
```

Create the local `.env` file from `.env.example`; never commit the filled file.

## Required secrets and configuration

Set secrets through the hosting platform's environment-variable/secret interface, not in source code, the Docker image, screenshots, chat, or public logs. Configuration may include role-specific API keys, `AUDITCHAIN_ED25519_SEED`, `AUDITCHAIN_ACTOR_HMAC_SECRET`, a database URL and credentials, and optional AI/Telegram credentials. Use unique, strong values and rotate any value that may have been exposed.

The Ed25519 seed must remain stable across restarts if the same signing identity is required. Existing checkpoints created under a previous key do not become verifiable under a new key.

`AUDITCHAIN_ACTOR_HMAC_SECRET` keys the pseudonyms in public actor IDs. Keep it stable; changing it changes the pseudonym shown for the same API key.

Roles: OFFICER submits evidence; REVIEWER decides review items; AUDITOR also creates checkpoints; ADMIN can do everything, including the demo-only `/api/reset`, `/api/tamper` and `/api/generate` routes. Any mutation not explicitly granted to another role is ADMIN-only. Keep `AUDITCHAIN_DEMO_ENABLED=false` in deployed environments.

## External PostgreSQL

The Render service uses `AUDITCHAIN_STORE_TYPE=postgres`, `SPRING_PROFILES_ACTIVE=postgres` and the existing Supabase connection details, stored only as Render environment variables. Do not reset or recreate the existing schema; apply only pending Flyway migrations.

Before claiming durable persistence:
1. Submit a synthetic evidence record and keep its receipt.
2. Confirm the record and chain entry are readable.
3. Restart or redeploy the Render service.
4. Confirm the same record, receipt and chain state remain.
5. Verify checkpoint recovery.

## Checkpoints and external anchors

The workflow `.github/workflows/anchor-checkpoints.yml` requests a signed checkpoint from the deployed service and appends it to `audit-anchors/anchors/log.jsonl`, retrying so a cold start does not fail the run.

`GET /api/checkpoints/verify` is public and read-only. It verifies the checkpoints stored in the chain and fetches the public anchor log, comparing each signed checkpoint with the live chain hash at that sequence. A missing, malformed, unreachable or mismatching log makes the result `valid: false`. The endpoint is rate limited (20 requests per minute per client, 120 globally) because each call makes one outbound request. `GET /api/chain` returns the validity verdict for the whole chain with a window of entries (default newest 200, `limit` up to 1000, `offset` to page).

The workflow uses the repository variable `AUDITCHAIN_BASE_URL` and secret `AUDITCHAIN_AUDITOR_KEY`. See [ANCHORING.md](ANCHORING.md) for the optional independent mirror.

## Load testing

The `Load test (manual)` workflow runs `scripts/load-test.py` from a GitHub runner against the live service. Start it from the Actions tab. Record the date, commit and output in EVALUATION.md.

## Post-deployment verification checklist

- [ ] Latest intended commit is deployed and the service is live.
- [ ] `GET /health` returns the expected response.
- [ ] CI is green for the exact final submission commit.
- [x] Protected routes reject missing or wrong keys (automated test).
- [x] Scheduled signed checkpoint anchoring and public append are working.
- [x] External PostgreSQL connection and Flyway migrations confirmed in logs.
- [ ] Synthetic evidence survives an application restart (direct test).
- [ ] `GET /api/checkpoints/verify` returns `valid: true` on the live deployment.
- [ ] Temporary admin key blanked or rotated.
- [ ] Logs do not expose secrets or sensitive payloads.
- [ ] Submission and accessibility flows tested on the deployed UI by hand.

## Rollback

If a deployment fails, stop relying on it, inspect Render build and runtime logs, and roll back to the last known-good deployment. Re-run health and chain verification afterwards. A rollback does not recover data lost from an ephemeral filesystem.

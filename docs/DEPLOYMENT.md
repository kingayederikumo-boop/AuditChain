# Deployment Guide

**Project:** AuditChain  
**Status:** Prototype deployment guide  
**Last reviewed:** 2026-11-02

## Current deployment posture

- Application: Java 17 / Spring Boot 3, containerized with the repository Dockerfile.
- Hosting: Render web service `auditchain`; the project is intended to remain on the existing free hosting arrangement.
- Cold start: the free instance took about 81 seconds to start in the Render logs of 2026-11-01. The first request after an idle period can take that long, so warm the service before any demonstration.
- Database: the existing Supabase project named **AuditChain** is connected to the Render service using PostgreSQL. Deployment logs of 2026-11-02 confirmed the PostgreSQL store, a successful connection and five validated Flyway migrations (schema version 4, up to date). No new database should be created.
- A synthetic write/read test across an application restart is still required before claiming durable production persistence.
- The application supports SQLite by default and PostgreSQL through the `postgres` profile. Do not assume SQLite on an ephemeral host is durable.
- Scheduled signed-checkpoint anchoring is operational: the scheduled GitHub Actions workflow appends a valid signed checkpoint to the public `audit-anchors` branch every six hours. At the 2026-11-01 review the public log held nine records (sequences 2 to 10). This branch is in the same repository and is not an independent trust domain.
- Direct live HTTP smoke checks and accessibility testing remain outstanding. A successful scheduled anchor run does not replace those checks.

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

Create the local `.env` file from `.env.example`; never commit the filled file. Confirm that the configured container port matches the application and hosting environment.

## Required secrets and configuration

Set secrets through the hosting platform's environment-variable/secret interface, not in source code, the Docker image, screenshots, or public logs. Depending on enabled features, configuration may include role-specific API keys, `AUDITCHAIN_ED25519_SEED`, `AUDITCHAIN_ACTOR_HMAC_SECRET`, a database URL and credentials, and optional AI/Telegram credentials. Use unique, strong values and rotate any value that may have been exposed.

The Ed25519 seed must remain stable across restarts if the same signing identity is required. Do not publish the seed. Existing checkpoints created under a previous ephemeral key do not automatically become verifiable under a new key.

`AUDITCHAIN_ACTOR_HMAC_SECRET` keys the pseudonyms that appear in public actor IDs. Keep it stable; changing it changes the pseudonym shown for the same API key. It was set on the Render service on 2026-11-02.

## External PostgreSQL

The existing AuditChain Supabase PostgreSQL project is the configured external database. Keep using it; do not create a second database or a paid Render database.

The Render service should use:
- `AUDITCHAIN_STORE_TYPE=postgres`
- `SPRING_PROFILES_ACTIVE=postgres`
- the existing Supabase PostgreSQL connection URL, username, and password stored only as Render environment variables/secrets.

Use Supabase's recommended connection mode for the Render network and comply with its TLS/SSL requirements. Do not paste database passwords into issues, chat, source files, or this document. Do not reset or recreate the existing schema. Apply only pending repository Flyway migrations.

Before claiming durable persistence:
1. Submit a synthetic evidence record and retain its receipt.
2. Confirm the record and chain entry are readable.
3. Restart or redeploy the Render service.
4. Confirm the same record, receipt, and chain state remain available.
5. Verify checkpoint recovery and review any provider backup/recovery limitations.

The existing connection and migrations have been observed in deployment logs; the restart-persistence test remains pending.

## Checkpoints and external anchors

The scheduled workflow in `.github/workflows/anchor-checkpoints.yml` requests a signed checkpoint from the deployed service and appends it to `audit-anchors/anchors/log.jsonl`. The request retries so that a cold-starting free instance does not fail the run.

`GET /api/checkpoints/verify` is a public, read-only check. It verifies the checkpoints stored in the chain and also fetches the public anchor log, comparing every signed checkpoint with the live chain hash at that checkpoint's sequence. A missing, empty, malformed, unreachable or mismatching log makes the result `valid: false`, with details in `externalAnchorCount`, `externalAnchorsValid` and `externalAnchorError`. Because it does not rely only on checkpoint entries stored inside the chain, a history rewrite that also removes those entries is still detected. The log source is set with `AUDITCHAIN_ANCHOR_LOG_URL`. The integrity console exposes the same check. Each call makes one outbound request to the anchor log host.

The workflow uses the repository variable `AUDITCHAIN_BASE_URL` and secret `AUDITCHAIN_AUDITOR_KEY`. Do not expose the auditor key. See [ANCHORING.md](ANCHORING.md) for setup, the optional independent mirror and verification.

A branch in the same repository is useful for a public append log but is not an independent trust domain: repository administrators can alter both branches. Mirroring to a separately administered repository is supported but not claimed until `AUDITCHAIN_ANCHOR_REPO` and its token secret are configured.

## Post-deployment verification checklist

- [ ] Latest intended commit is deployed and the service is live.
- [ ] `GET /health` returns the expected healthy response.
- [ ] CI is green for the exact final submission commit.
- [ ] Authentication is enabled for protected endpoints.
- [x] Scheduled signed checkpoint anchoring and public append are working.
- [x] Existing external PostgreSQL connection and Flyway migrations were confirmed in deployment logs.
- [ ] Synthetic evidence survives an application restart.
- [ ] `GET /api/checkpoints/verify` returns `valid: true` and `externalAnchorsValid: true` on the live deployment.
- [ ] Logs do not expose secrets or sensitive payloads.
- [ ] Submission and accessibility flows have been tested on the deployed UI.

## Rollback

If a deployment fails, stop relying on the affected deployment, inspect Render build and runtime logs, and roll back to the last known-good deployment. Re-run health and chain verification after rollback. A rollback does not recover data lost from an ephemeral filesystem.

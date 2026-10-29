# Deployment Guide

**Project:** AuditChain  
**Status:** Prototype deployment guide  
**Last reviewed:** 2026-10-30

## Current deployment posture

- Application: Java 17 / Spring Boot 3, containerized with the repository Dockerfile.
- Hosting: Render web service `auditchain`, free plan, health-check path `/health`.
- Current configured storage default: SQLite.
- A managed/external PostgreSQL connection is **not yet configured** for the deployed service.
- Render free hosting may use ephemeral storage; SQLite data on an ephemeral disk must not be treated as durable across restarts or redeploys.
- The latest code deployment must be checked in Render and through the live health endpoint before describing it as healthy.

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

Set secrets through the hosting platform's environment-variable/secret interface, not in source code, the Docker image, screenshots, or public logs. Depending on enabled features, configuration may include role-specific API keys, `AUDITCHAIN_ED25519_SEED`, a database URL and credentials, and optional AI/Telegram credentials. Use unique, strong values and rotate any value that may have been exposed.

The Ed25519 seed must remain stable across restarts if the same signing identity is required. Do not publish the seed. Existing checkpoints created under a previous ephemeral key do not automatically become verifiable under a new key.

## External PostgreSQL

PostgreSQL is supported by the application and local Docker Compose setup, but this repository does not currently contain a configured external database URL or credentials. To connect an existing external PostgreSQL service without creating a paid Render database:

1. Choose an existing database provider/account and confirm its free-tier limits, sleep/expiry behavior, backup policy, and connection limits.
2. Create a dedicated database/user with least privilege; do not use an owner or personal admin credential for the application.
3. Apply the repository's Flyway migrations using the application's documented PostgreSQL profile.
4. Configure the service with `AUDITCHAIN_STORE_TYPE=postgres`, the appropriate Spring datasource URL, username, and password as hosting secrets, and the PostgreSQL profile required by the app.
5. Confirm TLS/SSL requirements from the provider and use the provider's supported connection mode.
6. Deploy, inspect startup/migration logs, create a synthetic record, restart/redeploy, and verify the record and chain remain available.
7. Test backups and recovery before claiming production-grade durability.

Do not paste database passwords into issues, chat, source files, or public documentation. No database has been provisioned by this guide.

## Checkpoints and external anchors

See [ANCHORING.md](ANCHORING.md). The scheduled GitHub workflow requires the repository variable `AUDITCHAIN_BASE_URL` and repository secret `AUDITCHAIN_AUDITOR_KEY`. Configure these directly in GitHub Settings. Never place the auditor key in a public issue or commit.

A branch in the same repository is useful for publishing an audit trail but is not an independent trust domain. For stronger assurance, publish signed anchors to a separately controlled system or organization.

## Post-deployment verification checklist

- [ ] Latest intended commit is deployed and the service is live.
- [ ] `GET /health` returns the expected healthy response.
- [ ] CI is green for the exact commit.
- [ ] Authentication is enabled for protected endpoints.
- [ ] Signing public key is available and the private seed is secret.
- [ ] External PostgreSQL is configured and migrations complete, if persistent cloud storage is required.
- [ ] Synthetic evidence survives a restart when persistence is claimed.
- [ ] Checkpoint creation and historical verification succeed.
- [ ] Logs do not expose secrets or sensitive payloads.
- [ ] Submission and accessibility flows have been tested on the deployed UI.

## Rollback

If a deployment fails, stop relying on the affected deployment, inspect Render build and runtime logs, and roll back to the last known-good deployment. Re-run health and chain verification after rollback. A rollback does not recover data lost from an ephemeral filesystem.

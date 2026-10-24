# AuditChain

**Tamper-evident, offline-tolerant audit trail for simulated electoral results transmission**

#AI4Elections Hackathon 2026 – Track 4: Electoral Cybersecurity and Resilience  
Optional cross-cutting: Climate Resilience (offline operation under connectivity/power disruption)

## What it does

Simulates the flow of results from Polling Unit → LGA → State.  
Every entry is cryptographically linked (SHA-256 hash chain).  
Any modification of a past entry breaks the chain and is immediately detectable.  
Works offline: entries are queued locally (SQLite) and can be synchronised later.

This is a **simulation only**. It does not connect to, probe, or depend on any live electoral system.

## Tech stack (locked)

- Java 17+
- SHA-256 via JCA
- SQLite (offline) + PostgreSQL (online, later)
- Maven
- Minimal verification UI (later)
- Docker (later)

## Current status (Phase 0 → Phase 1)

- [x] Core `HashChainEngine` + `AuditEntry` model
- [x] In-memory chain create / verify / tamper detection demo
- [x] SQLite persistence layer
- [x] Offline queue (PENDING_SYNC status)
- [ ] Synthetic data generator
- [ ] STRIDE threat model document
- [ ] Verification UI
- [ ] Spring Boot packaging + Docker

## Quick start

```bash
# Build
mvn clean package

# Run the demo (in-memory + persistence demo)
mvn exec:java -Dexec.mainClass="com.auditchain.core.HashChainDemo"
```

## Project layout

```
src/main/java/com/auditchain/
  core/          HashChainEngine, demo
  model/         AuditEntry
  persistence/   SQLite store + offline queue
  service/       (future)
  web/           (future verification UI)
```

## Team

Two-person team. Core strength: Distributed Voting System experience on Hedera + backend/security.

## Licence

Hackathon prototype – not for production use.

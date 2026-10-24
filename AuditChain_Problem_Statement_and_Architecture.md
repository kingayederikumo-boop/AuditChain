# AuditChain
**Tamper-Evident, Offline-Tolerant Audit Trail for Simulated Electoral Results Transmission**

**#AI4Elections Hackathon 2026 – Track 4: Electoral Cybersecurity and Resilience**  
**Optional Cross-Cutting Area: Climate Resilience and Environmental Innovation**

---

## 1. Problem Statement

### The Challenge
In electoral processes, the integrity of results as they move from polling units through successive levels of collation is critical. Any undetected alteration, whether accidental or malicious, undermines public trust. Traditional paper trails and basic digital logs often lack strong cryptographic guarantees and struggle in low-connectivity environments common across many parts of Nigeria.

These connectivity and power challenges are frequently worsened by climate-related events (flooding, extreme weather, damaged infrastructure). Systems that depend on continuous network access become fragile exactly when reliable results transmission is most needed.

### Who It Serves
- Electoral officials responsible for results transmission and collation
- Independent auditors and election observers
- Civil society organisations monitoring process integrity
- Technology teams designing resilient electoral systems

### The Gap
Existing digital systems frequently rely on centralised databases or continuous network connectivity. When connectivity fails (including climate-induced outages), or when logs can be silently modified, detecting tampering becomes difficult or impossible after the fact. There is a need for a lightweight, offline-capable mechanism that makes any change to the results trail cryptographically evident and continues to function under disrupted conditions.

### Our Solution: AuditChain
AuditChain provides a **simulated** results transmission pipeline (Polling Unit → LGA → State) protected by a hash-chained, tamper-evident audit log. Every entry cryptographically links to the previous one. Any modification breaks the chain and is immediately detectable. The system is designed to function offline, queue entries locally, and synchronise when connectivity returns — without ever connecting to live electoral infrastructure.

By prioritising offline operation and cryptographic integrity, AuditChain also contributes to **climate resilience**: it remains usable during power and connectivity disruptions that climate events often cause.

This is a defensive tool and authorised simulation only. It does not interact with, probe, or depend on any real electoral system.

---

## 2. Methodology

### Approach
1. **Simulated Transmission Flow** — PU → LGA → State
2. **Hash-Chained Audit Log** — SHA-256, previousHash + canonical payload
3. **Offline-First Design** — local queue, sync when connectivity returns
4. **Tamper Detection** — verifyChain returns first broken sequence
5. **Threat Modelling** — STRIDE + residual risks (see docs/STRIDE_Threat_Model.md)
6. **Human Oversight** — system flags; humans decide

### Evaluation
- Chain integrity on clean logs
- Detection rate under controlled tampering
- Offline operation capability
- Threat model quality and residual-risk honesty
- Demo under simulated connectivity loss

### Data
Fully synthetic. No real electoral data or live systems.

### Responsible AI & Security Posture
- No automated decisions affecting real people or real results
- Clear residual risks
- No scanning/probing of external systems
- Defensive integrity only

---

## 3. Technical Architecture

### Stack (locked)
- Java 17+ / Spring Boot 3 (packaging later)
- SHA-256 via JCA
- SQLite (offline) + PostgreSQL (online, later)
- Docker
- Minimal React/HTML verification UI

### Core components
- `HashChainEngine` — pure in-memory chain create/verify
- `AuditEntry` — model with roles, status, canonical string for hashing
- `SqliteAuditStore` — durable offline store + PENDING_SYNC queue
- `PersistentHashChainEngine` — load on start, append, verify, markSynced

### Data flow (simulated)
1. PU officer records results → entry appended, status PENDING_SYNC
2. Offline period → chain continues locally
3. Connectivity returns → entries marked SYNCED in order
4. LGA / State nodes append their collation entries linked to prior tip
5. Auditor runs verifyChain → empty = valid; sequence number = first break

### Deployment posture (hackathon)
- **Backend / API:** Render or Railway (Spring Boot JAR / Docker). Not Vercel for the Java service.
- **Verification UI:** Static HTML/React on Vercel or same host as API.
- **Database:** SQLite file for offline demo nodes; managed Postgres on Render/Railway for central simulator.
- **Local demo:** `mvn exec:java` runs fully offline with in-memory or file SQLite.

Full residual-risk and STRIDE detail: `docs/STRIDE_Threat_Model.md`.

---

## 4. What we will not do
- Connect to, probe, or load-test any live electoral system
- Use real voter or results data
- Claim absolute security or production readiness
- Hide residual risks

---

*Prototype for #AI4Elections Hackathon 2026. Simulation only.*

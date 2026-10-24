# AuditChain

**Tamper-evident, offline-tolerant audit trail for simulated electoral results transmission**

**Live demo:** https://auditchain.onrender.com  

#AI4Elections Hackathon 2026 – **Track 4: Electoral Cybersecurity and Resilience**  
Optional cross-cutting: **Climate Resilience** (offline operation under connectivity / power disruption)

---

## What it does

Simulates results flow **Polling Unit → LGA → State**.  
Every entry is linked with **SHA-256** to the previous entry.  
Any modification of a past entry **breaks the chain** and is reported at the exact sequence number.  
Entries can be recorded with status `PENDING_SYNC` (offline queue) and marked synced later.

**Simulation only.** No live electoral systems, no real voter data.

---

## Quick start

### Live

1. Open https://auditchain.onrender.com  
2. Wait if the free tier is cold-starting (up to ~60 seconds).  
3. **Generate 5 PUs** → green **Chain VALID**.  
4. **Tamper entry 2** → red **Chain BROKEN at sequence 2**.

### Local

```bash
mvn -DskipTests package
PORT=8080 java -jar target/auditchain-0.1.0-SNAPSHOT.jar
```

Open http://localhost:8080

---

## API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | Liveness |
| GET | `/api/chain` | Full chain + validity |
| POST | `/api/generate?puCount=5` | Synthetic flow |
| POST | `/api/tamper?sequence=2` | Simulate attack |
| POST | `/api/reset` | Reseed clean chain |

---

## Docs

- [Architecture](./AuditChain_Problem_Statement_and_Architecture.md)
- [STRIDE threat model](./docs/STRIDE_Threat_Model.md)
- [Demo script (ElevenLabs + clicks)](./docs/DEMO_SCRIPT.md)
- [Submission checklist](./docs/SUBMISSION_CHECKLIST.md)

## Residual risks (honest)

1. Full rewrite of the only offline copy needs an external tip-hash checkpoint.  
2. Actor identity is synthetic — no signatures yet.  
3. Compromise at creation time can inject false but valid-looking entries.  
4. Demo host in-memory storage resets on restart/sleep.  
5. Does not protect live elections — defensive simulation only.

## Licence

Hackathon prototype — not for production use.

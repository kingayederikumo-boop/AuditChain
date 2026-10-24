# AuditChain – STRIDE Threat Model

**Scope:** Simulated results transmission (PU → LGA → State) with hash-chained, offline-tolerant audit trail.  
**Out of scope:** Live electoral systems, real voter data, offensive tooling.  
**Date:** 2026-10-24  
**Status:** Working draft for #AI4Elections Track 4

---

## 1. System under analysis

| Component | Role |
|-----------|------|
| HashChainEngine | Creates and verifies SHA-256 linked entries |
| SqliteAuditStore | Offline persistence + PENDING_SYNC queue |
| PersistentHashChainEngine | Glue: append → store → verify → sync |
| Actor roles | PU_OFFICER, LGA_OFFICER, STATE_OFFICER, AUDITOR (synthetic) |
| Verification UI (planned) | Read-only chain view and break-point display |

Trust boundary: everything is synthetic and local/simulated. No external INEC or third-party electoral API is called.

---

## 2. STRIDE analysis

### S – Spoofing

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Fake actorId / role injected into an entry | False attribution of who recorded results | ActorId and role are part of the canonical string that is hashed. Changing them after the fact breaks the chain. | At creation time, a compromised device can still claim any synthetic identity. No strong PKI binding yet. |
| Impersonation of a higher-level node during “sync” | Lower-level data accepted as State-level | Sync is offline queue mark only in current MVP; no network trust yet. Future: mutual auth + signed batches. | High until signed artefacts and node identity are added. |

**Decision:** Keep actor fields inside the hash. Document that identity is synthetic and not cryptographically bound in Phase 1.

---

### T – Tampering

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Modify payload of a past entry on disk | Silent alteration of results | `verifyChain()` recomputes every hash and returns the first broken sequence number. Demo already detects this. | Attacker who controls both storage *and* the verification binary can lie about the result. |
| Drop or reorder entries | Incomplete or reordered trail | Sequence numbers + previousHash links. Gaps or reordering break the chain. | Physical destruction of the offline device loses data (availability, not integrity of remaining chain). |
| Replace entire chain with a fabricated one | Full history rewrite | External auditor can keep an independent tip hash (or export) at known checkpoints. | Without external checkpoints, a fully offline rewrite is possible if the only copy is compromised. |

**Decision:** Primary guarantee is *detection*, not prevention. Residual risk is stated honestly: offline full-copy replacement requires external tip anchoring for stronger assurance.

---

### R – Repudiation

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Actor denies having created an entry | Accountability gap | Entry contains actorId, role, timestamp, locationCode, all inside the hash. | No digital signature yet → cannot prove *who* held the key; only that the record is consistent. |
| Clock manipulation on device | Wrong chronology | Timestamp is hashed; changes break the chain. Relative order still protected by sequence + previousHash. | Absolute time is only as good as the device clock at creation. |

**Decision:** Phase 1 = non-repudiation of *content integrity*. Phase 2 = optional signatures for stronger non-repudiation of actor.

---

### I – Information disclosure

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Leak of synthetic results payloads | Low (synthetic only) | No real PII. Local SQLite file permissions. | If real data were ever used (disallowed), encryption-at-rest would be required. |
| Exposure of chain tip hashes | Low | Tip hashes are integrity anchors, not secrets. | Public tip hashes are desirable for external verification. |

**Decision:** No encryption-at-rest in MVP (synthetic data). Document that production use would require it.

---

### D – Denial of service

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Fill local disk with junk entries | Offline node stops accepting data | Sequence and storage limits can be added; not critical for simulation scale. | Low for hackathon scope. |
| Connectivity loss | Cannot sync | *By design* offline queue continues; sync when network returns. Climate resilience feature. | Sync backlog growth under prolonged outage. |
| Corrupt SQLite file | Local chain unreadable | Backup exports of tip hash + periodic chain dump. | Single-device failure loses local history unless replicated. |

**Decision:** Offline operation is a feature, not a failure mode. Document residual risk of single-device loss.

---

### E – Elevation of privilege

| Threat | Impact | Mitigation | Residual risk |
|--------|--------|------------|---------------|
| Local process gains ability to rewrite store and claim “verified” | False assurance | Verification is a pure function of the stored chain. UI must not trust a “verified” flag that is not recomputed. | If attacker controls the verification process, all bets are off (same as any local tool). |
| Role confusion (PU writing as STATE) | Misleading audit trail | Role is hashed; policy checks can reject illegal role transitions in a later service layer. | Policy layer not yet implemented. |

**Decision:** Verification always recomputes. No trusted “isVerified” bit without recompute.

---

## 3. Residual risks (honest summary)

1. **Full offline rewrite** of the only copy of the chain cannot be detected without an external tip hash or independent checkpoint.  
2. **Actor identity** is synthetic and not cryptographically bound (no signatures / PKI in Phase 1).  
3. **Device compromise** at creation time can inject false payloads that still form a valid chain.  
4. **Single-device loss** is an availability risk; integrity of remaining devices is unaffected.  
5. **No live system interaction** by design — residual risk of “does not protect real elections today” is accepted and stated.

These are intentional Phase 1 boundaries, not oversights.

---

## 4. Mitigations already implemented

- SHA-256 hash chaining with canonical string  
- `verifyChain()` returns first broken sequence number  
- Offline queue with PENDING_SYNC / SYNCED status  
- Durable SQLite store with reload on restart  
- Synthetic data only; no live system contact  

## 5. Mitigations planned (Phase 1–2)

- Synthetic data generator (multi-node PU→LGA→State)  
- Minimal verification UI that always recomputes  
- Optional entry signatures (actor keypair)  
- Export of tip hash for external anchoring  
- Basic role-transition policy checks  

---

## 6. Climate resilience link

Connectivity and power loss (including climate-driven outages) are treated as expected operating conditions. The offline queue and local chain continue to provide tamper-evident records until sync is possible. Residual risk under prolonged isolation is backlog growth and single-device loss, not silent integrity failure.

---

*This document is part of the AuditChain submission for #AI4Elections Track 4. It prioritises clarity of residual risk over claims of absolute security.*

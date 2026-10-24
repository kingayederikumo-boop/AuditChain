# AuditChain – Demo Video Script

**Target length:** 3–5 minutes  
**Voice:** ElevenLabs (neutral, clear, confident — not hype)  
**Live URL:** https://auditchain.onrender.com  
**Fallback:** local JAR → http://localhost:8080

---

## Pre-roll (before record)

1. Open https://auditchain.onrender.com (wait up to ~60s if cold).  
2. Click **Reset** once.  
3. Confirm green **Chain VALID**.  
4. Start screen recording; play or sync ElevenLabs VO in edit.

---

# PART A — Voice only (paste into ElevenLabs)

Read straight through. Pauses are marked with `[pause]`.

---

**Segment 1 — Problem**

In Nigerian elections, results move from polling units to LGA to state. Digital logs can be altered without detection, and connectivity often fails — including during climate-related outages. AuditChain is a simulation only. It never touches live electoral systems. It shows how a hash-chained, offline-tolerant audit trail makes tampering obvious.

`[pause]`

---

**Segment 2 — What you are looking at**

This is the verification UI. Each entry is cryptographically linked to the previous one with SHA-256. Status PENDING_SYNC means the entry was recorded offline and is waiting to synchronise. The tip hash is the current end of the chain. If anything in the past is changed, verification fails at the exact sequence number.

`[pause]`

---

**Segment 3 — Generate**

I generate a fresh synthetic flow: five polling units, then LGA aggregation, then state. All data is fake. Parties and vote counts are random but consistent up the chain.

`[pause — wait for UI after Generate]`

---

**Segment 4 — Tamper (core)**

Now I simulate an attack. I tamper with entry number two — the kind of silent change that should never go unnoticed. Watch the banner and the highlighted row.

`[pause — wait for red banner]`

The system does not delete history. It detects the break. That is the integrity guarantee: detection, not magic prevention.

`[pause]`

---

**Segment 5 — Offline and residual risk**

Entries are designed to queue while offline and sync later. On the free demo host, the chain is in memory, so a restart resets state — that is intentional for this simulation, not a production deployment. Residual risks we document honestly: no cryptographic actor signatures yet; a full rewrite of the only offline copy needs an external tip-hash checkpoint; compromise at the moment of recording can still inject false but valid-looking entries. We state those limits instead of hiding them.

`[pause]`

---

**Segment 6 — Close**

AuditChain is a Track Four prototype for electoral cybersecurity and resilience, with a climate angle: offline operation under connectivity loss. Simulation only. Code and threat model are on GitHub. Live demo at auditchain.onrender.com.

---

### ElevenLabs notes

- Pace slightly slower than normal speech.  
- If misread: "P U", "L G A", "S H A two fifty-six".  
- No hype words ("unbreakable", "revolutionary").

---

# PART B — Clicks only (operator / editor cue sheet)

No voice text here. Do these on screen while the matching segment audio plays.

| Time (approx) | Segment | Action |
|---------------|---------|--------|
| 0:00 | 1 Problem | Browser on live URL. Green **Chain VALID** visible. No click. |
| 0:35 | 2 UI tour | Scroll entry list slowly. Hover **Latest hash** and **Pending sync**. No click. |
| 1:10 | 3 Generate | Click **Generate 5 PUs**. Wait until green **Chain VALID**. Short scroll of new entries. |
| 1:50 | 4 Tamper | Click **Tamper entry 2**. Hold 2+ seconds on red **Chain BROKEN at sequence 2**. Point at entry #2 attack payload. |
| 2:50 | 5 Residual | Optional: click **Reset**. Optional: open `/health` in another tab briefly. |
| 3:40 | 6 Close | Prefer **Reset** → end on green **VALID**. Show URL in address bar. |

---

## Edit checklist

- [ ] Cold start cut or labelled "warming up"  
- [ ] Tamper: 2+ seconds of red banner  
- [ ] Residual risks heard in audio  
- [ ] URL on screen at start and end  
- [ ] Total under 5 minutes  

---

*Part A = ElevenLabs input. Part B = what you click while recording or in the edit timeline.*

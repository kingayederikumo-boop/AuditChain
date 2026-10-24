# AuditChain – Demo Video Script

**Target length:** 3–5 minutes  
**Voice:** ElevenLabs (neutral, clear, confident — not hype)  
**Live URL:** https://auditchain.onrender.com  
**Fallback:** local JAR then http://localhost:8080

---

## Pre-roll (before record)

1. Open https://auditchain.onrender.com (wait through cold start if needed — up to ~60s).  
2. Click **Reset** once so the chain is clean.  
3. Confirm green **Chain VALID**.  
4. Start screen recording, then start voiceover (or record VO separately and sync in edit).

---

## Script (voice + clicks)

### Segment 1 — Problem (0:00–0:35)

**VO:**  
In Nigerian elections, results move from polling units to LGA to state. Digital logs can be altered without detection, and connectivity often fails — including during climate-related outages. AuditChain is a simulation only. It never touches live electoral systems. It shows how a hash-chained, offline-tolerant audit trail makes tampering obvious.

**Clicks:** None. Optional: show browser on the live URL, green VALID banner visible.

---

### Segment 2 — What you are looking at (0:35–1:10)

**VO:**  
This is the verification UI. Each entry is cryptographically linked to the previous one with SHA-256. Status PENDING_SYNC means the entry was recorded offline and is waiting to synchronise. The tip hash is the current end of the chain. If anything in the past is changed, verification fails at the exact sequence number.

**Clicks:**  
- Slowly scroll the entry list (PU_OFFICER rows).  
- Point cursor at **Latest hash** and **Pending sync** (no click required).

---

### Segment 3 — Generate clean flow (1:10–1:50)

**VO:**  
I generate a fresh synthetic flow: five polling units, then LGA aggregation, then state. All data is fake. Parties and vote counts are random but consistent up the chain.

**Clicks:**  
1. Click **Generate 5 PUs**.  
2. Wait for green **Chain VALID**.  
3. Brief scroll to show new entries and updated counts.

---

### Segment 4 — Tamper detection (1:50–2:50)  core moment

**VO:**  
Now I simulate an attack. I tamper with entry number two — the kind of silent change that should never go unnoticed. Watch the banner and the highlighted row.

**Clicks:**  
1. Click **Tamper entry 2**.  
2. Pause 2 seconds on red **Chain BROKEN at sequence 2**.  
3. Point at entry #2 payload showing the attack values.  
4. Note that later entries are still listed, but the chain is invalid from the break point.

**VO (continue):**  
The system does not delete history. It detects the break. That is the integrity guarantee: detection, not magic prevention.

---

### Segment 5 — Offline and residual risk (2:50–3:40)

**VO:**  
Entries are designed to queue while offline and sync later. On the free demo host, the chain is in memory, so a restart resets state — that is intentional for this simulation, not a production deployment. Residual risks we document honestly: no cryptographic actor signatures yet; a full rewrite of the only offline copy needs an external tip-hash checkpoint; compromise at the moment of recording can still inject false but valid-looking entries. We state those limits instead of hiding them.

**Clicks:**  
- Optional: click **Reset** to restore VALID.  
- Optional: open `/health` in a second tab for one second (UP).

---

### Segment 6 — Close (3:40–4:10)

**VO:**  
AuditChain is a Track Four prototype for electoral cybersecurity and resilience, with a climate angle: offline operation under connectivity loss. Simulation only. Code and threat model are on GitHub. Live demo at auditchain.onrender.com.

**Clicks:**  
- End on green VALID after Reset, or on the broken state if you want the last frame to be detection. Prefer **Reset to VALID** for a calm close.

---

## ElevenLabs notes

- Pace: slightly slower than conversational; leave gaps after button clicks for UI to update.  
- Spell out: "P U", "L G A", "S H A two fifty-six" if the model misreads.  
- Do not add marketing adjectives ("revolutionary", "unbreakable").

## Edit checklist

- [ ] Cold start cut out or labelled "warming up"  
- [ ] Tamper moment has 2+ seconds of red banner on screen  
- [ ] Residual risks spoken, not only on a slide  
- [ ] URL shown on screen at start and end  
- [ ] Total under 5 minutes  

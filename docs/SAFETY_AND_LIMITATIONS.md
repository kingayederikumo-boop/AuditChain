# Safety, Privacy and Limitations Statement

**Project:** AuditChain  
**Last reviewed:** 2026-11-20

## 1. Compliance with hackathon rules

- [x] No institutional or electoral system accessed or tested. The only live system tested is this project's own service.
- [ ] To confirm with the organisers: a load test was run against this project's own free-tier Render service (`EVALUATION.md`, part D). The Track 4 brief lists load-testing any live, institutional or third-party system as out of scope. It will not be repeated until the organisers answer.
- [x] Only synthetic or public data: the project's own generated records and the organisers' starter pack.
- [x] Nonpartisan. No persuasion or microtargeting.
- [x] Human oversight for consequential decisions. The check only routes sheets to a reviewer.

## 2. Risk register

| Risk | Who could be harmed | Likelihood | Impact | Mitigation | Residual |
|---|---|---|---|---|---|
| False positive: honest sheet flagged | Polling-unit officer, voters in that unit | Medium on real data | Low to medium | Flag wording says "needs review"; reviewer decides; flags never say fraud | A delay and possible stigma |
| False negative: consistent falsification | The public, candidates | High (by design not detected) | High | Signed checkpoints catch later edits; photo and review exist; documents say so | Wrong first figure stays unseen |
| Bias against a language or group | Users of other languages | Medium | Medium | Five languages; plain wording | Translations unreviewed |
| Privacy breach or re-identification | Officers, observers | Low | Medium | No personal data by design; HMAC pseudonyms for actor IDs | Pseudonym key must stay secret |
| Misuse by bad actors | Officers, observers | Low in prototype | High | Role-based keys, rate limits, demo routes admin-only | Temporary admin key still in use |
| Adversarial manipulation of the record | The public | Medium | High | Hash chain plus signed external checkpoints | Anchor log is in the same repository |
| Over-reliance on a clean result | The public | Medium | High | Home page and How it works state what a clean result does not prove | Users may still over-trust it |

## 3. Privacy and data protection

No personal data is collected. Records hold synthetic figures, a polling-unit code and a pseudonymous actor ID made with an HMAC. Access is by role key. There is no retention schedule yet, and the chain is append-only, so deleting entries from a live chain is itself detectable. See `PRIVACY.md`.

## 4. Known limitations

- Detects later edits, not a wrong first entry.
- The image reader is a mock. Photos are stored but not read.
- Consistently falsified sheets are not detected by the rules.
- Free hosting: sleeps when idle, slow under load, process-local rate limits.
- Translations unreviewed. Ijaw missing. No screen-reader or real-phone test yet.
- The anchor log sits in the same repository, so it is not independent of the operator.

## 5. Conditions for further testing

Written authorisation from the relevant institution, independent security review and penetration test, a data-protection assessment, native-speaker review of translations, accessibility testing with real users, an evaluation on a labelled sample with natural errors, and an independently administered anchor mirror. See `PATHWAY.md`.

## 6. Statement

"This prototype is not approved for operational electoral deployment."

Signed: ______________ (team representative)   Date: ____________

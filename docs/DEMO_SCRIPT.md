# Demo Script (2 minutes)

**Audience:** hackathon judges and electoral-integrity practitioners.  
**Rule:** synthetic data only; say "simulated" on screen and aloud.  
**Target length:** 2:00. Rehearse to 1:50.

## 0:00 – 0:15 The problem

"When results sheets move from polling unit to collation, anyone can quietly change a figure and nobody can prove it. AuditChain makes every change visible."

*Screen:* console home. Point at the Records intact status.

## 0:15 – 0:45 Submit and flag

*Screen:* submit page. Submit a synthetic sheet whose party figures do not add up to its declared total.

"A polling-unit officer submits a simulated result sheet. The system checks the arithmetic. It does not accuse anyone: it sends the sheet to a human reviewer, and records who did what under a pseudonym."

*Show:* the sheet appearing as needs review.

## 0:45 – 1:15 Tamper, then detect

*Screen:* the isolated tamper demo from `docs/TAMPER_DEMO.md` on a disposable local copy.

"Now someone edits one stored record. The chain breaks at exactly that record. In 100 of 100 test edits, verification caught it."

## 1:15 – 1:45 Why signed anchors matter

*Screen:* console Verification section, then the public anchor log.

"A clever attacker rewrites the whole chain so it looks consistent. The chain alone cannot see that, and our tests show it: zero of 100. But every few hours a signed checkpoint is published publicly. Against that anchor, all 100 rewrites are caught. Thirty-one signed checkpoints are public now, and anyone can check them."

## 1:45 – 2:00 Close

"Honest limits: this is a prototype on synthetic data; it checks arithmetic, not images; the anchor log should move to an independent host. It is built to run on a phone and low bandwidth. AuditChain: evidence you can check, not trust."

## Before recording

- Warm the live service first (cold start is about 80 seconds on the free tier).
- Use only synthetic sheets; never show an API key on screen.
- Re-read `docs/EVALUATION.md` and quote only figures that appear there.

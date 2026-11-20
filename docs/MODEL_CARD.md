# System Card: AuditChain screening and tamper-evident record

## Summary

AuditChain stores simulated election result sheets in a hash-linked record, publishes signed checkpoints so later edits can be detected, and screens each sheet with simple arithmetic and plausibility rules. Flagged sheets go to a human reviewer. Version: 0.1.0 prototype. Date: 2026-11-20. Author: the AuditChain project owner (individual entry).

There is no trained model. The default image-reading provider is a mock that reads the figures typed in, not the photo. An optional HTTP provider can be configured but has not been evaluated.

## Intended use

Primary users: polling-unit officers (submit), reviewers (decide on flagged sheets), observers and journalists (verify). Intended task: show that saved records were not changed after saving, and surface inconsistent sheets for people to check.

**Out of scope:** declaring results, deciding that fraud occurred, deciding the validity of votes, persuasion, profiling individuals, or use on real election data or live electoral systems.

## Training and data

No training. The rules are fixed. Test data: the organisers' official synthetic pack (900 records) and the project's own seeded synthetic sheets and chains. All fictional. See `DATASHEET.md`.

## Evaluation

| Slice | n | Result | Notes |
|---|---|---|---|
| Official pack, all records | 900 | 42 of 42 anomalies flagged, 0 of 858 clean flagged | `EVALUATION_OFFICIAL_DATA.md` |
| Own sheets, altered declared totals | 60 | 60 flagged | `EVALUATION.md`, part A |
| Own sheets, clean | 100 | 0 flagged | `EVALUATION.md`, part A |
| Tamper detection, naive edits | 100 | 100 detected by the chain | `EVALUATION.md`, part B |
| Consistent rewrite | 100 | 0 detected by the chain alone, 100 detected against the signed tip | `EVALUATION.md`, part B |

No confidence intervals beyond those in the evaluation documents. No comparison with an independent human baseline.

## Error analysis

- False negatives: consistently falsified sheets, sheets with no figures to compare, anything the figures do not reveal. A missed sheet means a wrong figure goes unreviewed. The affected party is the public and any candidate harmed.
- False positives: honest sheets with round or repeated numbers, or a typing slip by an officer. A false flag costs a reviewer a few minutes and may delay a sheet. It must not be treated as an accusation.

## Fairness and bias

The rules look only at numbers, not at party names, places or languages. They have not been tested by region, polling-unit size or group. Round-number and repeated-figure rules could fire more often in very small units. The interface translations (Pidgin, Yoruba, Igbo, Hausa) have not been reviewed by native speakers.

## Uncertainty and abstention

The rules give a yes or no, with the reason codes that fired. They do not give a confidence score. A blank or unreadable payload is always sent to a human.

## Human oversight

The review queue holds flagged sheets. A reviewer's decision is recorded as a separate entry from the automatic check, and both are on the chain with pseudonymous actor IDs. The check never states that fraud occurred.

## Ethical and legal considerations

Nonpartisan. Synthetic data only. No personal data is collected by design (see `PRIVACY.md`), which is consistent with the Nigeria Data Protection Act 2023. Main misuse risks: treating a flag as proof, or treating a clean chain as proof that figures are true. The interface and documents say neither is the case.

## Maintenance

Prototype, no monitoring or retraining. Issues: the GitHub repository `kingayederikumo-boop/AuditChain`.

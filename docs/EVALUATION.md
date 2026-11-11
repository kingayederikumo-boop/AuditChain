# Evaluation Plan and Results

**Project:** AuditChain  
**Scope:** Defensive, synthetic-data prototype for the #AI4Elections Track 4 submission  
**Last reviewed:** 2026-11-11

## Evaluation questions

1. Does verification accept an unchanged chain and identify tampering at the first invalid sequence?
2. Does checkpoint verification compare signed historical checkpoints with the corresponding historical chain hashes?
3. Does evidence submission return a stable receipt for repeated idempotent requests?
4. Does the application preserve review and AI event attribution in the audit chain?
5. Can the submission UI queue work while offline and retry when connectivity returns?
6. Are authorization failures, malformed requests, and unavailable optional integrations handled safely?
7. Are the documented limitations observable and accurately represented in the demo?

## Test method

Run the repository's CI command from a clean checkout:

```bash
mvn -B clean verify
```

The seeded harness `src/test/java/com/auditchain/evaluation/EvaluationHarnessTest.java` prints lines starting with `EVAL|` so every figure below can be reproduced from the CI log. Use synthetic fixtures only. Do not test against a live electoral system or real election data.

## Measured results (automated, synthetic)

Source: commit `2be512fd3d411ab177b486433d14fd6eb8ad536b`, GitHub Actions run `42924579941`, 2026-11-10, JDK 17, `mvn -B verify`: **44 tests, 0 failures, 0 errors, 0 skipped**. Random seeds are fixed (2026 and 7).

### A. Arithmetic screening of 200 synthetic result sheets

| Sheet type | Count | Flagged for human review | Rate |
|---|---|---|---|
| Clean (declared total equals party sum) | 100 | 0 | 0.0% false-flag |
| Altered declared total | 60 | 60 | 100.0% detection |
| of which off by exactly one vote | 20 | 20 | 100.0% detection |
| Blank payload | 20 | 20 | 100.0% sent to a human |
| No declared total | 20 | 0 | not detectable |

Scope and limits: this checks arithmetic consistency of a declared payload only. It cannot detect a sheet whose party figures and total were both falsified consistently, and it cannot judge a sheet that declares no total. It never declares fraud; a flag only routes the sheet to a human reviewer. The vision provider used here is the deterministic mock; no claim is made about image-reading accuracy.

### B. Tamper detection, 200 trials on a 30-entry chain

| Attacker capability | Trials | Detected by chain verification alone | Detected against the signed anchored tip |
|---|---|---|---|
| Naive edit (payload, actor, location or previous hash) without recomputing | 100 | 100 (100.0%) | n/a |
| Rewrite newest entry and recompute its hash | 50 | 0 | 50 (100.0%) |
| Rewrite an entry and recompute every later hash | 50 | 0 | 50 (100.0%) |

This is the design rationale for signed external checkpoints: a hash chain by itself detects careless tampering but not a consistent rewrite. Only a previously published, signed tip exposes the rewrite. The anchored comparison in this harness checks the recorded tip hash; signature validity is covered by `CheckpointServiceTest` and the offline check below.

### C. Authorization matrix (`ApiKeyAuthFilterTest`)

Public reads need no key; the review queue needs REVIEWER or AUDITOR; evidence submission needs OFFICER or AUDITOR; checkpoint creation needs AUDITOR; `/api/reset`, `/api/tamper` and `/api/generate` are ADMIN-only. Verified by automated test (5 tests, all passing).

## Other evidence

- The scheduled signed-checkpoint workflow publishes to the public `audit-anchors` branch. An offline check on 2026-11-10 verified all 31 published Ed25519 signatures under a single public key (31 of 31 valid). This shows the publication path works; it is not independent third-party immutability because the branch is in the same repository.
- Deployment logs confirmed a PostgreSQL connection and Flyway migrations to version 4. Chain sequence numbers continued across repeated service restarts, which is indirect evidence of persistence; a direct restart test is still to be recorded.
- Earlier, the tamper demonstration in `docs/TAMPER_DEMO.md` was run against a disposable local database.

## Not yet done

No load test, penetration test, accessibility audit, image-reading accuracy evaluation or user study has been completed. None of those figures are claimed.

## Reporting rules

Do not invent accuracy percentages, latency figures, detection rates, uptime, or user-study outcomes. For every reported result, record the commit SHA, test command, environment, timestamp, total/passed/failed/skipped counts, and any limitations. Separate automated unit/integration results from manual and production results.

## Acceptance criteria for submission

- CI succeeds on the exact final submission commit.
- Checkpoint regression tests and the evaluation harness are included and passing.
- Demo uses only synthetic records and clearly labels simulation-only operations.
- Any failed, skipped, or unavailable test is disclosed.
- Live deployment health and storage persistence are reported as verified only after direct checks.

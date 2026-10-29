# Evaluation Plan and Results

**Project:** AuditChain  
**Scope:** Defensive, synthetic-data prototype for the #AI4Elections Track 4 submission  
**Last reviewed:** 2026-10-30

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

Use synthetic fixtures only. Do not test against a live electoral system or real election data.

| Area | Method | Evidence to retain |
|---|---|---|
| Chain integrity | Verify an unchanged chain; alter a stored payload/hash in a controlled test fixture and verify rejection | Test output and first failing sequence |
| Historical checkpoints | Create a checkpoint, extend the chain, then mutate the historical hash in a test fixture | Regression test output |
| Idempotency | Submit the same synthetic evidence with the same idempotency key twice | Receipt comparison |
| Authorization | Call protected routes without a key, with a wrong key, and with each permitted role | HTTP status and route matrix |
| AI and review | Exercise mock AI path and reviewer decision flow | AI_CHECK and REVIEW_DECISION event evidence |
| Offline queue | Disable network in a controlled browser test, queue synthetic submission, restore network, and observe retry | Browser/device notes and request results |
| Deployment | Check health endpoint, service logs, persistent storage behavior, and restart recovery | Timestamped deployment record |
| Accessibility | Execute the checklist in ACCESSIBILITY.md | Completed test matrix |

## Results currently evidenced

- The repository's CI runs `mvn verify` on pushes to `main`.
- At the last implementation checkpoint on 2026-10-29, a Render build reported 30 tests, 0 failures, 0 errors, and 4 skipped. This is a historical build result, not proof that every later commit or the current live deployment is healthy.
- Regression tests were added for historical checkpoint verification.
- No independent AI accuracy/fairness evaluation, penetration test, accessibility audit, or load test is documented as complete.

## Reporting rules

Do not invent accuracy percentages, latency figures, detection rates, uptime, or user-study outcomes. For every reported result, record the commit SHA, test command, environment, timestamp, total/passed/failed/skipped counts, and any limitations. Separate automated unit/integration results from manual and production results.

## Acceptance criteria for submission

- CI succeeds on the exact submission commit.
- Checkpoint regression tests are included and passing.
- Demo uses only synthetic records and clearly labels simulation-only operations.
- Any failed, skipped, or unavailable test is disclosed.
- Live deployment health and storage persistence are reported as verified only after direct checks.

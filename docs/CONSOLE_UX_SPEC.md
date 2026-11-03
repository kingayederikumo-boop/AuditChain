# AuditChain Console UX Specification

**Status:** Implementation proposal / first console pass  
**Scope:** Public, read-only monitoring console for the synthetic AuditChain prototype.

## Product statement

AuditChain helps preserve submitted electoral-result records, detect changes to recorded history, and make evidence available for review. It is a defensive simulation. It is not a voting system, an INEC integration, or a certification that submitted results are truthful.

## Console goals

The first screen must answer, in plain English:

1. Can the record chain currently be checked?
2. Did the local chain integrity check pass?
3. Do published signed checkpoints match the current chain?
4. How many records and pending-sync items are currently reported?
5. Is there an integrity issue or an incomplete verification that needs attention?

## Primary navigation

| Destination | Purpose | Current implementation |
|---|---|---|
| Overview | Overall integrity, key counts, alerts | Implemented in the static console |
| Records | Recent audit-chain entries and technical details | Read-only view backed by `GET /api/chain` |
| Verification | Local chain and published checkpoint status | Backed by `GET /api/chain` and `GET /api/checkpoints/verify` |
| Submit evidence | Open the submission workflow | Links to `/submit.html` |

The first console pass does not expose a Review Centre because the repository has review APIs but does not yet provide a complete reviewer-facing page and authenticated browser session. It does not expose administrator controls or the climate scenario as if they were public monitoring features.

## Status rules

- **Verified:** The local chain passes, at least one published checkpoint exists, and public checkpoint verification passes.
- **Integrity issue detected:** The local chain reports a broken link. Display the reported break sequence when available.
- **Checkpoint mismatch:** Public checkpoints exist but the comparison fails. Treat this as an investigation-required condition; do not claim a fraud verdict.
- **Partial / independent verification incomplete:** The local chain passes but no usable published checkpoint history was confirmed.
- **Unavailable:** The relevant endpoint could not be reached. An unavailable check is not evidence of tampering.

A green local-chain result alone must never be described as complete independent verification.

## Data sources

| Displayed information | Source |
|---|---|
| Record count, local chain status, break sequence, latest hash, pending sync, entries | `GET /api/chain` |
| Stored-checkpoint result, public checkpoint count, external-anchor result and errors | `GET /api/checkpoints/verify` (`internalCheckpointValid`, `internalCheckpointError`, `externalAnchorCount`, `externalAnchorsValid`, `externalAnchorError`) |
| Submission workflow | `/submit.html` |
| Evidence arithmetic flags and AI review signals | Parsed evidence payloads in `GET /api/chain` plus `GET /api/review/pending` |

The console must render server-returned values only. It must not invent sample counts, incidents, successful verifications, or climate conditions.

## Integrity alert behavior

- A broken local chain produces a prominent integrity alert with the break sequence, if supplied.
- A failed stored-checkpoint comparison produces an integrity alert even if the public anchor log is unavailable.
- A failed comparison when published checkpoints exist produces a checkpoint-mismatch alert.
- A passing local chain with no usable external checkpoint history produces an amber incomplete-verification notice, not a tampering alert.
- Endpoint failures produce an unavailable state, not a compromised state.
- A successful check is described narrowly: it means the available checks passed, not that the original result was true.

## Evidence review visibility

- The overview shows a dedicated **Evidence needing attention** section and a count of records requiring attention.
- A deterministic arithmetic mismatch is computed by comparing the sum of declared party figures with `totalVotes`, `total_votes`, or `votes_cast`.
- AI review signals are associated with their audit-chain evidence entry ID; the AI event and pending-review item use that same ID.
- Arithmetic mismatches, AI flags, and pending human review are shown as review signals. None is described as tampering or fraud.
- The public console remains read-only. Reviewer decisions continue to require the authenticated reviewer API and are not available from this page.

## Scope boundaries and known gaps

- The public console is read-only. Role-protected reviewer and administrator actions must not be exposed without a proper authenticated UI.
- Review queue APIs exist, but a reviewer-facing workflow still needs implementation and role-aware authentication.
- Climate disruption is a simulation-only capability, not a live climate-risk feed or a claim about field power availability.
- Pending-sync count reflects the backend's current report. The browser's offline submission queue is device-local and is not necessarily represented by this server count.
- External anchors in the same repository are useful for a public append log but are not an independent trust domain.
- The prototype uses synthetic or expressly authorized data only.

## Acceptance checks

1. A healthy local chain with valid published checkpoints displays **Verified**.
2. A broken local chain displays a prominent integrity issue and its break sequence.
3. A public checkpoint mismatch displays a distinct investigation-required warning.
4. Missing, empty, or unreachable public checkpoints display **Partial** or **Unavailable**, never a false tampering verdict.
5. Counts and records are populated from live API responses only.
6. Search filters the visible record list without changing backend data.
7. The navigation works on mobile and desktop, and keyboard focus remains visible.
8. The page does not expose API keys or perform reviewer/admin actions.
9. A mismatch between declared party figures and the sheet total appears in the attention list with the actual figures.
10. AI flags and pending-review items are linked to the corresponding chain entry and never labelled as fraud.

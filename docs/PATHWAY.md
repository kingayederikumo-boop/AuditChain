# Pathway to real-world use

**Project:** AuditChain  
**Status:** Plan, not a claim. Today AuditChain is a prototype on synthetic data.

## The problem it addresses

Result sheets move from polling unit to collation to state level. After a figure is recorded, anyone with access to a database or a portal can change it, and nobody outside can prove it either way. AuditChain makes the history of those records checkable: every record is hash-linked to the one before it, and signed checkpoints are published where the operator cannot quietly rewrite them.

## Who would run it

- **Operator:** an election management body, or an accredited citizen-observer coalition running a parallel record of the same sheets.
- **Submitters:** polling-unit officers or observers with a phone, using the offline-tolerant submit page.
- **Reviewers:** staff or observers who decide on sheets the arithmetic check flags.
- **Verifiers:** anyone. The public verification and the anchor log need no login.

## What it does and does not do

| It does | It does not |
|---|---|
| Show that recorded history was not altered after the fact | Prove the first figure entered was true |
| Flag sheets whose party figures do not add up to the declared total | Detect a consistently falsified sheet, or read images reliably (the default checker is arithmetic only) |
| Route flagged sheets to a human reviewer and keep the AI out of fraud decisions | Replace legal complaint, tribunal or collation procedures |
| Work on low bandwidth through an offline queue | Guarantee delivery on a dead network |

## Phases

1. **Simulated pilot (months 0-3).** Run a mock election day with students or observers on synthetic sheets. Measure submission success, review turnaround, time to verify, and usability in the five supported languages. Fix what breaks.
2. **Hardening (months 3-6).** Move the signing key into a managed key service, publish checkpoints to at least two independently administered places, add a real image-reading model only after it is evaluated for accuracy and fairness, and commission an independent security review and penetration test.
3. **Observer deployment (months 6-12).** Parallel record of real sheets by a consenting observer group, with a data-protection assessment, retention rules and an agreed incident process.
4. **Integration.** Offer a read-only verification feed that existing result portals could display, without changing how those portals work.

## What would have to change first

- **Signing key custody:** today the key comes from a configured seed. Production needs hardware or managed key storage and a rotation plan.
- **Independent anchors:** the anchor log is already public and archived with the Internet Archive; a second, separately administered mirror is supported but not yet configured.
- **Image reading:** the default checker is arithmetic only. Any model must be evaluated on realistic sheets across lighting and handwriting before use.
- **Data protection:** a lawful basis, retention and deletion rules, and a data-protection impact assessment under Nigerian law, with legal advice, before any real data.
- **Identity of submitters:** keys are per role today. A field deployment needs per-officer credentials that can be revoked.
- **Scale and cost:** the prototype runs on free hosting and sleeps when idle. A pilot needs always-on hosting, a managed database with backups, and a measured load test.

## Costs

The prototype runs on free tiers. Real costs depend on hosting, database size, SMS or data costs for field users, key management and the security review. None of these have been priced, and no figure is claimed here.

## Main risks

| Risk | Mitigation |
|---|---|
| Operator rewrites history and the anchor log together | Independent anchors and public archive copies |
| False confidence: "records intact" read as "results are true" | Plain-language scope notice on every screen |
| AI flags read as accusations | Flags only route to humans; wording says they are not findings of fraud |
| Phones offline for long periods | Offline queue and idempotent retry |
| Translation errors on safety statements | Native-speaker review before any pilot |
| Abuse of public endpoints | Rate limits and pagination, to be load-tested |

## How success would be measured

Share of sheets recorded within a set time; share of flagged sheets reviewed within a set time; verification success from an outside party; time for a new officer to submit their first sheet; and the number of false flags, tracked and published.

# Responsible AI Statement

**Project:** AuditChain  
**Use case:** AI-assisted review within a simulated electoral-results audit trail  
**Status:** Prototype; not approved for operational election use  
**Last reviewed:** 2026-10-30

## Purpose and boundaries

AuditChain explores whether a tamper-evident audit trail and offline-tolerant workflow can support review of synthetic electoral-results evidence. It is a defensive demonstration, not an election-management system, a voter-authentication system, or a fraud-detection authority.

## Human oversight

- AI output is advisory and may be wrong, incomplete, biased, or inconsistent.
- AI must not autonomously declare that fraud occurred.
- Human reviewers retain responsibility for review decisions.
- Review decisions are recorded separately from AI output so that the two are distinguishable.
- A chain entry demonstrates integrity of recorded content, not the truth of the content itself.

## Data and model limits

- Use synthetic data only.
- The default vision integration is a mock provider; real-model performance has not been established by that mock.
- An optional HTTP provider can be configured. Operators must assess the provider's data handling and model limitations before sending any evidence to it.
- No accuracy, fairness, or real-election effectiveness claim is made by this prototype.

## Prohibited uses

Do not use AuditChain to:
- process real voter or election-result records;
- make or automate decisions about voter eligibility, candidates, vote validity, or election outcomes;
- identify, profile, or target voters or election workers;
- claim that a hash chain alone proves the correctness of an entry or the identity of its author;
- connect to, probe, disrupt, or test live electoral infrastructure without explicit authorization.

## Risk controls

- Role-based API keys restrict access to selected operations.
- SHA-256 hash chaining and Ed25519 checkpoints help make later alteration detectable when verification material is trustworthy.
- AI checks and human review decisions are represented as distinct audit events.
- The project maintains a threat model and documents residual risks.
- Synthetic-only scope and simulation labels are used to avoid implying that a scenario is a real event.

## Known residual risks

- A compromised operator, application, signing key, or storage layer may undermine trust.
- A hash chain does not establish factual correctness or independent actor identity.
- Without independently controlled external anchors, a complete rewrite of the only stored chain may be difficult to detect.
- The deployed default SQLite store on ephemeral hosting is not reliable production persistence.
- AI evaluation against a representative, independently labelled dataset has not been completed.
- Rate limiting is process-local and does not provide distributed abuse protection.

## Governance

Before any expansion beyond the prototype, require a documented risk assessment, a human-oversight process, representative evaluation, data-protection review, accessibility testing, independent security review, incident response plan, and approval by the responsible project owner. Any use in a real election would require a separate authorization and substantially stronger assurance.

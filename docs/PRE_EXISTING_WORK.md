# Pre-existing Work and Attribution

**Project:** AuditChain  
**Entry:** #AI4Elections 2026, Track 4 (Electoral Cybersecurity and Resilience)  
**Last reviewed:** 2026-11-21

## What this page is for

This page says what is new work, what comes from other people, and what existed before. The project owner has confirmed each item in the checklist below.

## What is new

- The repository `kingayederikumo-boop/AuditChain` was created on 2026-10-23 (GitHub's own record).
- No AuditChain code, design or documentation existed before this project started.
- Nothing was reused from an earlier project.
- The repository holds the prototype, its documentation, tests, Docker setup and CI workflow.
- AuditChain is a simulated, defensive audit trail for synthetic result sheets. Every record is linked to the one before it by a hash, and it can be used offline.

## Who owns the work

This is a solo project. To the owner's knowledge, no teammate, mentor, employer, school or client has any rights in it.

## Other people's software

AuditChain uses open-source tools: Java, Spring Boot, Maven, SQLite and PostgreSQL drivers, Docker and GitHub Actions. Their own licences still apply. The full list is in `pom.xml`. The licences of the indirect dependencies still need a check before release.

This project does not claim to own any of them. The project licence covers only the code and text written for AuditChain.

## Data and AI

- The demo uses fictional test records only. No real election data is used.
- The built-in "AI" reader is a mock. It reads typed figures, not images.
- The only outside data is the official starter data, described in the update below.

## Owner checklist

- [x] No AuditChain code, design or documentation existed before this project began.
- [x] Nothing was reused from an earlier personal or team project.
- [x] This is an individual project. To the owner's knowledge, nobody else has rights in it.
- [x] The demo uses fictional test records and project-owned code only (see the update below for the starter data).
- [x] The commit history is an accurate record of the work. Nothing was imported from another source.

## Update, 2026-11-20

- **Official starter data.** After the hackathon began, two files from the organisers' public starter pack were copied unchanged into `src/test/resources/official-synthetic/`: `results_synthetic.csv` and `anomaly_labels_HELD_OUT.csv` (Rise Networks, CC BY 4.0). They are used only to test the screening rules. This replaces the earlier statement that no downloaded datasets were used.
- **AI tool help.** An AI assistant (Claude, by Anthropic) helped write and edit code, tests, documents and interface text. This includes the sheet rules, the evaluation tests, the redesigned pages and these documents. The owner wrote, edited, directed and reviewed the work.

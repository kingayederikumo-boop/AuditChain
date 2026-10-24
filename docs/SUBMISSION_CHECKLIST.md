# AuditChain – Submission checklist

## Product

- [x] Working hash chain + verify
- [x] Offline queue statuses (PENDING_SYNC / SYNCED)
- [x] Synthetic generator PU → LGA → State
- [x] Live demo URL
- [x] Tamper detection visible in UI
- [x] STRIDE + residual risks written

## Docs

- [x] README with live URL and residual risks
- [x] Problem / architecture note
- [x] Application short version
- [x] Demo script (voice + clicks)
- [ ] Demo **video** recorded and uploaded
- [ ] Application form submitted (confirm official deadline)

## Ops

- [ ] UptimeRobot (or similar) ping on `https://auditchain.onrender.com/health` every 10 minutes
- [ ] Cold-start tested once before recording video
- [ ] GitHub `main` builds (CI)

## Video acceptance criteria

- [ ] Under 5 minutes
- [ ] Shows VALID → Tamper → BROKEN at sequence 2
- [ ] States "simulation only"
- [ ] States at least two residual risks out loud
- [ ] Shows live URL on screen

## Do not do before submit

- Add blockchain "for show"
- Claim production readiness
- Touch any live electoral system
- Hide residual risks

# Checkpoint anchoring

The scheduled workflow in `.github/workflows/anchor-checkpoints.yml` requests a signed checkpoint from the deployed service every six hours and appends the returned signed record to the public `audit-anchors` branch at `anchors/log.jsonl`.

## Required GitHub configuration

In the repository settings:

- Add repository variable `AUDITCHAIN_BASE_URL` with the deployed service base URL (for example, `https://auditchain.onrender.com`).
- Add repository secret `AUDITCHAIN_AUDITOR_KEY` with the auditor API key configured in Render.
- Ensure Actions has permission to write repository contents.

Run the workflow manually once after configuring both values. Confirm a new record appears in the `audit-anchors` branch.

## Verification

Each record contains the server's signed checkpoint, including sequence, chain tip hash, timestamp, signature, and pinned public key. The workflow fails if the endpoint fails or the returned checkpoint is missing required fields or is not marked valid.

The `audit-anchors` branch is a public append log, but it is hosted in the same GitHub repository. It is not an independent trust domain: repository administrators can alter both branches. For stronger separation, mirror these records to a separately administered public repository or independent immutable storage.

## Operational limitations

- The workflow depends on the service being reachable and the auditor secret remaining valid.
- An anchor is only as trustworthy as the checkpoint signing key and the chain's own integrity checks.
- A newly configured signing seed stabilizes future signatures; it does not retroactively validate checkpoints signed by previous ephemeral keys.

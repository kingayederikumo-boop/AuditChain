# Checkpoint anchoring

The scheduled workflow in `.github/workflows/anchor-checkpoints.yml` requests a signed checkpoint from the deployed service every six hours and appends the returned signed record to `audit-anchors/anchors/log.jsonl`. Requests retry up to five times for transient network or server failures.

## Required GitHub configuration

In the AuditChain repository settings:

- Add repository variable `AUDITCHAIN_BASE_URL` with the deployed service base URL (for example, `https://auditchain.onrender.com`).
- Add repository secret `AUDITCHAIN_AUDITOR_KEY` with the auditor API key configured in Render.
- Ensure Actions has permission to write repository contents.

Run the workflow manually once after configuring both values. Confirm a new record appears in the `audit-anchors` branch.

## Independent mirror (recommended)

The `audit-anchors` branch is public, but it is hosted in the same repository as the application and is not an independent trust domain. Repository administrators can alter both branches.

To mirror anchors to a separately administered public repository:

1. Create a separate public repository, for example `auditchain-anchors`, and initialize it with a README.
2. In the AuditChain repository, set variable `AUDITCHAIN_ANCHOR_REPO` to `owner/auditchain-anchors`.
3. Add secret `AUDITCHAIN_ANCHOR_REPO_TOKEN` using a fine-grained token with Contents: Read and write access only to the anchor repository.
4. Set Render's `AUDITCHAIN_ANCHOR_LOG_URL` to the raw URL for the mirror's `anchors/log.jsonl`, for example `https://raw.githubusercontent.com/owner/auditchain-anchors/main/anchors/log.jsonl`.
5. Run the workflow manually and confirm the same signed record appears in both logs.

If the mirror settings are absent, the workflow reports that the independent mirror is not configured and retains the same-repository anchor branch as a fallback. The fallback is not independent protection.

## Verification

`GET /api/checkpoints/verify` checks checkpoints stored in the chain and fetches the configured external anchor log. Every external record's signature is verified against the service's trusted Ed25519 public key, then its checkpoint hash is compared with the live chain hash at the exact anchored sequence. A missing, empty, malformed, unavailable, or mismatching external log fails verification rather than silently passing.

Configure `AUDITCHAIN_ED25519_SEED` as a stable 32-byte seed encoded as 64 hexadecimal characters. Rotating or omitting it changes the signing identity; prior anchors will no longer verify against the new key.

## Operational limitations

- The workflow depends on the service being reachable and the auditor secret remaining valid; retry is bounded and does not fix a persistently unavailable service.
- An anchor is only as trustworthy as the checkpoint signing key and the chain's own integrity checks.
- A newly configured signing seed stabilizes future signatures; it does not retroactively validate checkpoints signed by previous ephemeral keys.
- The independent mirror is not active until its repository variable and token secret are configured.

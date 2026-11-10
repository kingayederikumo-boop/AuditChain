# Isolated Tamper Demonstration

**Purpose:** demonstrate that the in-memory hash-chain verifier detects a changed synthetic record.  
**Classification:** local demo only — not a production integrity test.

## Safety boundary

- Run this only on a local instance backed by a new, disposable SQLite file.
- Never enable demo mode on Render, a shared environment, a live database, or a real election system.
- The demo flag defaults to `false`. The `/api/generate`, `/api/tamper`, and `/api/reset` routes must remain unavailable in deployed environments.
- Use synthetic records only. A mismatch is evidence that the demo record changed; it is not evidence of fraud or a conclusion about any real election.

## Start an isolated instance

From a clean repository checkout, use one terminal:

```bash
mvn -B clean package
export AUDITCHAIN_STORE_TYPE=sqlite
export AUDITCHAIN_SQLITE_PATH=/tmp/auditchain-tamper-demo.db
export AUDITCHAIN_DEMO_ENABLED=true
java -jar target/auditchain-0.1.0-SNAPSHOT.jar
```

Use a path that does not exist yet. If you have used this exact path before, choose a different filename. Do not point it at the normal application data directory.

The demo-enabled application seeds an empty store with synthetic entries at startup. Keep this process local and disposable.

## Run the check

In a second terminal, from the repository root:

```bash
python3 scripts/verify-tamper-demo.py
```

Expected result:

1. The initial chain is valid and contains synthetic entries.
2. The script changes the payload of synthetic entry sequence 2 through the demo-only endpoint.
3. The next verification reports an invalid chain with `breakSequence: 2`.

The script exits non-zero if any expectation fails.

## What this proves — and does not prove

This demonstrates detection of an in-memory payload change by the current hash-chain verifier. The existing demo operation mutates the in-memory entry object; it does **not** rewrite the persisted database row. Restarting the local demo process reloads the unchanged stored chain, so the chain verifies again.

This is intentionally narrower than a persisted-storage tamper test. It does not prove protection against database administrators, filesystem-level modification, compromised application credentials, or an attacker who can rewrite both records and trusted checkpoints. Those require separate controlled fixtures and independent trust anchors.

After the demonstration, stop the local process and delete only the disposable `/tmp/auditchain-tamper-demo.db` file you created for this run.

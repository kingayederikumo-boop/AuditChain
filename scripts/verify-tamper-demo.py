#!/usr/bin/env python3
"""Verify the local, synthetic-only AuditChain tamper demonstration."""
import json
import os
import sys
import urllib.error
import urllib.request

BASE_URL = os.environ.get("AUDITCHAIN_BASE_URL", "http://127.0.0.1:8080").rstrip("/")


def request(path, method="GET"):
    req = urllib.request.Request(BASE_URL + path, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status, json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {path} returned HTTP {exc.code}: {body}") from exc


def main():
    status, before = request("/api/chain")
    assert status == 200 and before.get("valid") is True, "Initial synthetic chain must verify"
    assert before.get("size", 0) >= 2, "Demo chain must contain at least two entries"

    _, tampered = request("/api/tamper?sequence=2", method="POST")
    assert tampered.get("valid") is False, "Tamper operation should make verification fail"
    assert tampered.get("breakSequence") == 2, (
        f"Expected first invalid sequence 2, got {tampered.get('breakSequence')!r}"
    )

    _, after = request("/api/chain")
    assert after.get("valid") is False and after.get("breakSequence") == 2, (
        "A fresh chain read should continue to report the in-memory integrity failure"
    )
    print("PASS: synthetic demo chain initially verified.")
    print("PASS: changing synthetic entry 2 was detected at sequence 2.")
    print("LIMIT: this demo mutates in-memory state, not the persisted database row.")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        sys.exit(1)

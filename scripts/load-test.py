#!/usr/bin/env python3
"""Small stdlib-only load test for the public read endpoints.

Usage:
    python3 scripts/load-test.py https://auditchain.onrender.com --requests 300 --concurrency 20

It hits /health, /api/chain?limit=50 and /api/checkpoints/verify. /verify is rate limited
(20 per minute per client), so HTTP 429 there is expected and is counted separately, not as a failure.
Record the output, date and target in docs/EVALUATION.md; do not quote numbers that were not measured.
"""
import argparse
import statistics
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor

PATHS = ["/health", "/api/chain?limit=50", "/api/checkpoints/verify"]


def hit(url):
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(url, timeout=60) as resp:
            resp.read()
            code = resp.status
    except urllib.error.HTTPError as err:
        code = err.code
    except Exception:
        code = 0
    return url, code, (time.perf_counter() - start) * 1000


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("base")
    ap.add_argument("--requests", type=int, default=300)
    ap.add_argument("--concurrency", type=int, default=20)
    args = ap.parse_args()
    base = args.base.rstrip("/")

    print("warming up (cold start can take ~80 s)...")
    hit(base + "/health")

    urls = [base + PATHS[i % len(PATHS)] for i in range(args.requests)]
    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        results = list(pool.map(hit, urls))
    elapsed = time.perf_counter() - t0

    print(f"\n{args.requests} requests, concurrency {args.concurrency}, {elapsed:.1f}s, {args.requests / elapsed:.1f} req/s")
    for path in PATHS:
        rows = [r for r in results if r[0] == base + path]
        ok = [r for r in rows if r[1] == 200]
        limited = [r for r in rows if r[1] == 429]
        bad = [r for r in rows if r[1] not in (200, 429)]
        lat = sorted(r[2] for r in ok)
        p50 = statistics.median(lat) if lat else float("nan")
        p95 = lat[int(0.95 * (len(lat) - 1))] if lat else float("nan")
        print(f"{path:28} ok={len(ok):4} rate-limited={len(limited):4} errors={len(bad):4} p50={p50:7.0f}ms p95={p95:7.0f}ms")
    errors = sum(1 for r in results if r[1] not in (200, 429))
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()

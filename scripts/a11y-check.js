#!/usr/bin/env node
/*
 * Automated accessibility scan of the console and submit page.
 * Runs axe-core (WCAG 2.0/2.1/2.2 A and AA rules) in Chromium for every language, on a phone and a desktop
 * viewport, with the API mocked using synthetic data. Also reports horizontal overflow and the keyboard focus walk.
 *
 * Usage (from the repo root):
 *   npm i --no-save playwright axe-core@4.10.2
 *   node scripts/a11y-check.js
 *
 * This supplements, and does not replace, manual keyboard and screen-reader testing (docs/ACCESSIBILITY.md).
 */
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');
const http = require('http');

const staticDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'static');
const axeSrc = fs.readFileSync(require.resolve('axe-core/axe.min.js'), 'utf8');

const entry = (id, seq, loc, votes, total) => ({
  id, sequence: seq, role: 'OFFICER', location: loc, status: 'RECORDED', currentHash: 'bb...',
  timestamp: '2026-11-10T10:00:00Z',
  payload: JSON.stringify({ type: 'evidence', location: loc, data: { partyVotes: votes, totalVotes: total } })
});
const chain = { size: 3, valid: true, latestHash: 'deadbeefdeadbeef', pendingSync: 0, entries: [
  entry('1', 1, 'PU-001', { A: 10, B: 12 }, 22), entry('2', 2, 'PU-002', { A: 50, B: 40 }, 95)] };
const verify = { valid: true, chainSize: 3, checkpointCount: 2, internalCheckpointValid: true, externalAnchorCount: 31, externalAnchorsValid: true };

// Serves the static folder and injects the same tags as I18nScriptInjectionFilter.
const server = http.createServer((req, res) => {
  const p = req.url.split('?')[0];
  const file = path.join(staticDir, p === '/' ? 'index.html' : p.slice(1));
  if (!file.startsWith(staticDir) || !fs.existsSync(file)) { res.statusCode = 404; return res.end(); }
  const type = file.endsWith('.js') ? 'text/javascript' : file.endsWith('.css') ? 'text/css' : 'text/html; charset=utf-8';
  res.setHeader('Content-Type', type);
  let body = fs.readFileSync(file);
  if (type.startsWith('text/html')) {
    body = body.toString('utf8')
      .replace('</head>', '<link rel="stylesheet" href="/a11y.css"></head>')
      .replace('</body>', '<script src="/i18n.js" defer></script></body>');
  }
  res.end(body);
}).listen(0);

(async () => {
  const base = `http://127.0.0.1:${server.address().port}`;
  const browser = await chromium.launch();
  let runs = 0, failures = 0;
  for (const lang of ['en', 'pcm', 'yo', 'ig', 'ha']) {
    for (const pageName of ['index.html', 'submit.html']) {
      for (const vp of [{ n: 'phone', w: 360, h: 740 }, { n: 'desktop', w: 1280, h: 800 }]) {
        const ctx = await browser.newContext({ viewport: { width: vp.w, height: vp.h } });
        const page = await ctx.newPage();
        await page.route('**/api/chain*', r => r.fulfill({ json: chain }));
        await page.route('**/api/checkpoints/verify', r => r.fulfill({ json: verify }));
        await page.route('**/api/review/pending', r => r.fulfill({ status: 401, json: {} }));
        await page.addInitScript(l => { try { localStorage.setItem('auditchain-lang', l); } catch (e) {} }, lang);
        await page.goto(`${base}/${pageName}`);
        await page.waitForTimeout(700);
        await page.evaluate(axeSrc);
        const res = await page.evaluate(() => axe.run(document, { runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'] } }));
        const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth + 1);
        runs++;
        if (res.violations.length || overflow) {
          failures++;
          console.log(`FAIL ${lang} ${pageName} ${vp.n}: overflow=${overflow}`, res.violations.map(v => `${v.id}(${v.nodes.length})`).join(','));
        }
        await ctx.close();
      }
    }
  }
  const ctx = await browser.newContext({ viewport: { width: 360, height: 740 } });
  const page = await ctx.newPage();
  await page.goto(`${base}/submit.html`);
  const stops = [];
  for (let i = 0; i < 10; i++) {
    await page.keyboard.press('Tab');
    stops.push(await page.evaluate(() => { const e = document.activeElement; const cs = getComputedStyle(e); return `${e.tagName.toLowerCase()}${e.id ? '#' + e.id : ''}:${cs.outlineStyle}`; }));
  }
  console.log('keyboard focus order:', stops.join(' > '));
  await browser.close(); server.close();
  console.log(`${runs} runs, ${failures} with problems`);
  process.exit(failures ? 1 : 0);
})();

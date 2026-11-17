# Accessibility Statement and Test Plan

**Project:** AuditChain  
**Status:** Automated scan clean after the November redesign; manual testing still pending. Conformance is not certified.  
**Last reviewed:** 2026-11-17

## Goal

The prototype should be usable by people with varied vision, hearing, mobility, and cognitive access needs. The project uses WCAG 2.2 Level AA as a target for the web interface, but this document does not claim that the application conforms.

## Requirements

- Every interactive control must have a programmatic name and a visible label or equivalent accessible name.
- All core flows must be operable using a keyboard alone, with a visible focus indicator and a logical focus order.
- Text and controls must have sufficient contrast; information must not be conveyed by colour alone.
- Page structure must use meaningful headings, labels, landmarks, and status announcements.
- Form errors must identify the affected field and explain how to correct it.
- Dynamic verification, submission, and review states should be announced to assistive technology without forcing focus unexpectedly.
- Images and icons that convey meaning need appropriate text alternatives; decorative images should be ignored by assistive technology.
- Layout should remain usable when zoomed and on narrow/mobile viewports.
- Motion and time limits must not be necessary to understand an outcome.
- Plain language should explain integrity checks, errors, and whether a result is simulated. The pages can be switched between English, Naijá Pidgin, Yorùbá, Igbo and Hausa (see LANGUAGES.md).

## Design choices that support access

- Five separate pages (Check, Result sheets, How it works, How to use, Send a sheet) reached from a side menu, instead of one long page.
- The side menu is a button with `aria-expanded` and `aria-controls`. Opening it moves focus to the close button, Tab stays inside it, Escape closes it and returns focus to the menu button, and the page behind does not scroll while it is open.
- Onyx, royal blue and white palette with a dark mode that follows the device setting. Green, red, amber and grey are used only for results, and every result also has a text label.
- Plain wording instead of technical terms, and readable labels instead of raw role and status codes.
- Sheets entered without a network wait on the phone and can be sent later.

## Automated scan (2026-11-17, after the redesign)

Method: axe-core (WCAG 2.0, 2.1 and 2.2 A and AA rules) in Chromium through Playwright over all five pages in all five languages, at a phone viewport and a desktop viewport, in light and dark mode (100 runs), with the API mocked using synthetic data. It also checks for horizontal overflow and page errors. A separate test opens the side menu on every page and checks the focus behaviour above, with axe run on the open menu in light and dark.

Result: 0 axe violations, 0 horizontal overflow and 0 page errors in 100 of 100 runs; no side menu problems found.

Limits of this evidence: the 100-run harness was run locally and is not yet committed to the repository, so these figures cannot be reproduced from CI. The earlier script `scripts/a11y-check.js` covers only the first two pages. Automated rules find only part of the possible problems. The data was mocked, the pages were served from the repository's static files with the same tags the server injects, and only Chromium was used. The scan does not judge whether translations read correctly or whether the plain-language text is understandable.

### Earlier scan (2026-11-12, previous design)

That scan found low button contrast, missing form focus outlines, a top bar that overflowed a 360 px screen and controls under 44 px tall. All were fixed and the scan was clean; the redesign replaced the button colour and top bar, and the 2026-11-17 scan above supersedes it.

## Still to do (manual)

1. Screen-reader pass (TalkBack on Android) over submission, verification and review, including the side menu and announcements of status changes.
2. Keyboard-only run on the live deployment.
3. 200% zoom and system large-text settings on a real phone.
4. Read-through of each translation by a native speaker.
5. Real-device touch check on low-end Android.

Record browser/device, assistive technology, date, failures, and remediation for each test.

## Current status

Automated scan: clean as of the date above. Manual assistive-technology testing: not yet done. Treat the interface as **not verified conformant** until the manual tests are recorded. Do not describe the prototype as WCAG-conformant without that evidence.

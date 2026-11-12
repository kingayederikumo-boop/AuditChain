# Accessibility Statement and Test Plan

**Project:** AuditChain  
**Status:** Automated scan clean after fixes; manual testing still pending. Conformance is not certified.  
**Last reviewed:** 2026-11-12

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

## Automated scan (2026-11-12)

Method: `scripts/a11y-check.js` runs axe-core 4.10.2 (WCAG 2.0, 2.1 and 2.2 A and AA rules) in Chromium over the console and the submit page in all five languages, at a 360 x 740 phone viewport and a 1280 x 800 desktop viewport (20 runs), with the API mocked using synthetic data. It also checks for horizontal overflow, walks the keyboard focus order on the submit page and lists touch targets under 44 px.

| Finding before fixes | Where | Fix |
|---|---|---|
| Submit button text had insufficient contrast (white on `#5278ed`) | Submit page, 10 of 20 runs | Darker button colour (`#3d5fd0`) in `a11y.css` |
| Form fields had no visible focus outline (only a faint border/shadow) | Submit page | 2 px outline on `:focus-visible` for inputs, textarea and select |
| Top bar overflowed a 360 px screen (language selector plus refresh/back links) | Console 5 of 5 languages; submit page in Igbo and Hausa (7 of 20 runs) | Top bar wraps on small screens; live-status label hidden on phones |
| Several controls and the back link were under 44 px tall | Phone viewport | 44 px minimum height on small screens |

Result after fixes: 0 axe violations and 0 horizontal overflow in 20 of 20 runs; keyboard focus is visible on every stop in the walk; no touch target under 44 px on the submit page.

Limits of this evidence: automated rules find only part of the possible problems. The data was mocked, the page was served from the repository's static files with the same tags the server injects, and only Chromium was used. The scan does not judge whether translations read correctly or whether the plain-language text is understandable.

## Still to do (manual)

1. Screen-reader pass (TalkBack on Android) over submission, verification and review, including announcements of status changes.
2. Keyboard-only run on the live deployment.
3. 200% zoom and system large-text settings on a real phone.
4. Read-through of each translation by a native speaker.
5. Real-device touch check on low-end Android.

Record browser/device, assistive technology, date, failures, and remediation for each test.

## Current status

Automated scan: clean as of the date above. Manual assistive-technology testing: not yet done. Treat the interface as **not verified conformant** until the manual tests are recorded. Do not describe the prototype as WCAG-conformant without that evidence.

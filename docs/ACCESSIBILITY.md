# Accessibility Statement and Test Plan

**Project:** AuditChain  
**Status:** Accessibility requirements and manual test plan; conformance not yet certified  
**Last reviewed:** 2026-10-30

## Goal

The prototype should be usable by people with varied vision, hearing, mobility, and cognitive access needs. The project uses WCAG 2.2 Level AA as a target for the web interface, but this document does not claim that the current application conforms.

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
- Plain language should explain integrity checks, errors, and whether a result is simulated.

## Test plan

Before submission or release, test the actual deployed UI for:

1. Keyboard-only navigation through submission, verification, and review.
2. Visible focus, sensible focus order, and no keyboard traps.
3. Screen-reader names and status announcements for buttons, inputs, errors, and verification results.
4. Text and control contrast, including focus and disabled states.
5. 200% zoom and narrow-screen reflow without loss of functionality.
6. Form labels, required fields, and actionable error messages.
7. Image alternatives and non-colour indicators for chain-valid/invalid states.
8. Any offline-queue status and retry controls.
9. Touch-target usability on a mobile device.

Record browser/device, assistive technology, date, failures, and remediation for each test. Automated scans may supplement, but cannot replace, manual testing with keyboard and assistive technology.

## Current status and known gap

No completed accessibility audit or independent conformance assessment has been recorded in the repository as of this review. Treat accessibility as **not yet verified** until the test plan is executed and results are documented. Do not describe the prototype as WCAG-conformant without evidence.

# Languages

Every page has a language selector (top bar). The choice is remembered in the browser. The five pages are Check, Result sheets, How it works, How to use and Send a sheet.

## Why

The people who check results sheets, from polling-unit officers to observers and reviewers, work in the languages they speak daily. A tool that only works in English leaves them out. This is also part of the hackathon's inclusion and accessibility criterion.

| Language | Status |
|---|---|
| English | Source text |
| Naijá Pidgin | Draft translation, native-speaker review pending |
| Yorùbá | Draft translation, native-speaker review pending |
| Igbo | Draft translation, native-speaker review pending |
| Hausa | Draft translation, native-speaker review pending |
| Ịjọ (Ijaw) | Selector slot present but disabled: no dictionary yet; needs a native translator |

## How it works

`src/main/resources/static/i18n.js` translates text and the `placeholder`, `aria-label` and `title` attributes in the browser, including text the pages create after loading. It is key based: each piece of text has a key with one entry per language. `I18nScriptInjectionFilter` adds the shared stylesheet and the script tag to all five pages. Strings with no translation stay in English.

## Adding or fixing a language

Edit the key table in `i18n.js`. Each row is `[English, Pidgin, Yoruba, Igbo, Hausa]`. To enable Ijaw, add a dictionary and remove it from the disabled list.

## Limits

The browser confirmation dialog for deleting the offline queue is not translated. The server's own error messages stay in English. The wording was simplified in the November redesign (for example "fingerprint" and "Needs review"), and the Yoruba, Igbo and Hausa text for some of those terms still uses the older wording. Translations are drafts: a native speaker should review wording, especially the statements about fraud and prototype scope, before the final submission.

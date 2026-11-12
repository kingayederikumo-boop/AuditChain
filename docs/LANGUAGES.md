# Languages

The console and the submit page have a language selector (top bar). The choice is remembered in the browser.

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

`src/main/resources/static/i18n.js` translates text and the `placeholder`, `aria-label` and `title` attributes in the browser, including text the pages create after loading. `I18nScriptInjectionFilter` adds the script tag to the two pages. Strings with no translation stay in English.

## Adding or fixing a language

Edit the exact-string table and the pattern table in `i18n.js`. Yoruba, Igbo and Hausa share rows of `[English, Yoruba, Igbo, Hausa]`; Pidgin has its own tables. To enable Ijaw, add a dictionary and remove it from the disabled list.

## Limits

The browser confirmation dialog for deleting the offline queue is not translated. Role names and the server's own error messages stay in English. Translations are drafts: a native speaker should review wording, especially the statements about fraud and prototype scope, before the final submission.

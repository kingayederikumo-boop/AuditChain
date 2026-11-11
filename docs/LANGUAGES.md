# Languages

The console and the submit page have a language selector (top bar). Choice is remembered in the browser.

| Language | Status |
|---|---|
| English | Source text |
| Yorùbá | Draft translation, native-speaker review pending |
| Igbo | Draft translation, native-speaker review pending |
| Hausa | Draft translation, native-speaker review pending |
| Ịjọ (Ijaw) | Selector slot present but disabled: no dictionary yet; needs a native translator |

## How it works

`src/main/resources/static/i18n.js` translates text and the `placeholder`, `aria-label` and `title` attributes in the browser, including text the pages create after loading. `I18nScriptInjectionFilter` adds the script tag to the two pages. Strings with no translation stay in English.

## Adding or fixing a language

Edit the `X` table (exact strings) and the `P` table (strings with numbers in them) in `i18n.js`. Each row is `[English, Yoruba, Igbo, Hausa]`. To enable Ijaw, add a fourth column mapping `ijc` in `COL` and fill it in.

## Limits

The browser confirmation dialog for deleting the offline queue is not translated. Role names and the server's own error messages stay in English. Translations are drafts: a native speaker should review wording, especially the safety statements about fraud and prototype scope, before the final submission.

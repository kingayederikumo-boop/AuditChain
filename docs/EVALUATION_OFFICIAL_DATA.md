# Evaluation on the organisers' official synthetic data

**Project:** AuditChain  
**Track:** 4 (with Track 2 brief 2A as the reference for the checks)  
**Date:** 2026-11-20  
**Commit:** `9b0d75cc8018cf38b90ce83bd315f92f42092083`, CI run `44700932622` (passed)

## 1. Objective and success criteria

The Track 2 brief asks for validation of result-sheet style records for arithmetic and consistency errors, scored by precision and recall against injected anomalies and by reviewer workload (alerts per 1,000 records).

Question: do AuditChain's sheet rules flag the anomalies the organisers injected, without flagging clean records? Flags only send a sheet to a human reviewer. They never declare fraud.

## 2. Test data

- Source: `data/synthetic/results_synthetic.csv` and `anomaly_labels_HELD_OUT.csv` from the #AI4Elections Hackathon starter pack (Rise Networks), licence CC BY 4.0.
- Size: 900 fictional polling-unit records. The answer key lists 42 anomalies and 858 clean records.
- Copies: unchanged, in `src/test/resources/official-synthetic/` (see `ATTRIBUTION.md` there). SHA-256: results `7890642a...85a66`, key `acefc1b4...d1e5`.
- Held-out: the key is used only to score. The rules have no learned parameters and no thresholds, so nothing was fitted to the key.
- Honesty note: the rule set follows the anomaly names in the pack's data dictionary, and the organisers' own baseline uses the same four ideas. I looked at the data before writing the final test, so this is a check against the organisers' pack, not a blind test.

## 3. Baseline

Before this change the screening compared the party figures with a declared total. The pack has no declared-total field, so that check had nothing to compare and could not flag any of the 42 (true by construction, not separately measured). The new rules use the fields the pack does have.

## 4. Rules

| Rule | Fires when |
|---|---|
| `accredited_exceeds_registered` | accredited voters are more than registered voters |
| `votes_do_not_match_accredited` | valid votes plus rejected ballots do not equal accredited voters |
| `round_numbers` | every party figure is a multiple of 100 and not all are zero |
| `same_as_previous_unit` | the party figures are identical to the unit listed just before it (batch rule) |

## 5. Results

| Anomaly type in the key | Records | Flagged | Rule that fired |
|---|---|---|---|
| accredited_exceeds_registered | 16 | 16 | accredited_exceeds_registered (and votes_do_not_match_accredited) |
| suspicious_round_numbers | 12 | 12 | round_numbers (and votes_do_not_match_accredited) |
| sum_mismatch | 8 | 8 | votes_do_not_match_accredited |
| duplicated_from_previous_unit | 5 | 5 | same_as_previous_unit (and votes_do_not_match_accredited) |
| digit_transposition | 1 | 1 | votes_do_not_match_accredited |
| none (clean) | 858 | 0 | none |

| Measure | Value |
|---|---|
| Recall | 42 of 42 (100%). 95% Wilson lower bound about 91.6% |
| False flags on clean records | 0 of 858. 95% upper bound about 0.35% (rule of three) |
| Precision | 42 of 42 (100%) |
| Alerts per 1,000 records | 46.7, all true alerts in this pack |

How the figures were obtained: the test `OfficialDataEvaluationTest` asserts 42 anomalies, 42 flagged and 0 false flags, and CI passed. The per-type and per-rule counts above come from running the same `SheetRules` class on the same files in a standalone program (42 flagged by `votes_do_not_match_accredited`, 16 by `accredited_exceeds_registered`, 12 by `round_numbers`, 5 by `same_as_previous_unit`). The CI log did not show the printed lines, so the table is not copied from the log.

## 6. Human-in-the-loop impact

About 47 sheets per 1,000 would reach a reviewer in this pack, because 5% of it was made anomalous. This is a count of alerts, not a measurement with real reviewers.

## 7. Robustness and usability

Not tested here: typos, other languages, low bandwidth, adversarial input, or reviewers' own experience.

## 8. Failure cases and limits

- Every injected anomaly in this pack breaks `valid + rejected = accredited`, so that one rule finds all 42. The pack cannot show what the rules miss.
- A sheet whose figures were falsified so that they still add up is not detected. The 100% recall must not be read as field performance.
- `digit_transposition` has one example. A transposition that keeps the sum unchanged would not be flagged.
- `round_numbers` and `same_as_previous_unit` can fire on honest sheets in real data (small units, similar turnout). The brief warns about this base-rate problem. In real data, with a lower rate of true anomalies and natural errors, precision would be lower than shown.
- `same_as_previous_unit` only makes sense for an ordered list of units.

## 9. Reproducibility

```bash
mvn -B verify    # runs OfficialDataEvaluationTest and SheetRulesTest
```

No random seeds are used. The data is fixed. JDK 17.

## 10. Conclusion and next steps

The rules are a transparent first filter that routes inconsistent sheets to people. Before any real use they would need real sheet layouts, a labelled sample with natural errors, and reviewer testing to set alert levels. See `PATHWAY.md`.

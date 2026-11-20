package com.auditchain.evaluation;

import com.auditchain.ai.SheetRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Part C of the evaluation. Prints lines prefixed "EVAL|C|" so the numbers in docs/EVALUATION.md can be copied from
 * the CI log.
 *
 * Data: the official starter pack from the #AI4Elections organisers (see
 * src/test/resources/official-synthetic/ATTRIBUTION.md). 900 synthetic polling-unit records, about 5% with injected
 * anomalies, plus the held-out answer key. The key is used only to score the rules, never to build them.
 */
class OfficialDataEvaluationTest {

    @Test
    void officialSyntheticData() throws IOException {
        List<String[]> rows = readCsv("/official-synthetic/results_synthetic.csv");
        Map<String, String> label = new TreeMap<>();
        for (String[] l : readCsv("/official-synthetic/anomaly_labels_HELD_OUT.csv")) label.put(l[0], l[1]);
        assertEquals(900, rows.size());

        int truePos = 0, falsePos = 0, anomalies = 0, clean = 0, flagged = 0;
        Map<String, int[]> perType = new TreeMap<>();      // type -> {n, flagged}
        Map<String, Integer> ruleHits = new TreeMap<>();   // rule -> number of records it fired on
        int[] prev = null;

        for (String[] r : rows) {
            int registered = Integer.parseInt(r[1]);
            int accredited = Integer.parseInt(r[2]);
            int rejected = Integer.parseInt(r[8]);
            int[] votes = new int[5];
            for (int i = 0; i < 5; i++) votes[i] = Integer.parseInt(r[3 + i]);

            List<String> fired = new ArrayList<>(SheetRules.check(registered, accredited, rejected, votes));
            if (SheetRules.sameAsPrevious(prev, votes)) fired.add(SheetRules.SAME_AS_PREVIOUS);
            prev = votes;
            for (String f : fired) ruleHits.merge(f, 1, Integer::sum);

            String type = label.get(r[0]);
            boolean isAnomaly = !"none".equals(type);
            boolean isFlagged = !fired.isEmpty();
            if (isFlagged) flagged++;
            if (isAnomaly) {
                anomalies++;
                if (isFlagged) truePos++;
            } else {
                clean++;
                if (isFlagged) falsePos++;
            }
            int[] t = perType.computeIfAbsent(type, k -> new int[2]);
            t[0]++;
            if (isFlagged) t[1]++;
        }

        System.out.println("EVAL|C|records|" + rows.size() + "|anomalies in answer key|" + anomalies + "|clean|" + clean);
        for (Map.Entry<String, int[]> e : perType.entrySet()) {
            System.out.println("EVAL|C|type|" + e.getKey() + "|n|" + e.getValue()[0] + "|flagged|" + e.getValue()[1]);
        }
        for (Map.Entry<String, Integer> e : ruleHits.entrySet()) {
            System.out.println("EVAL|C|rule fired on|" + e.getKey() + "|" + e.getValue() + " records");
        }
        System.out.println("EVAL|C|recall|" + pct(truePos, anomalies) + "|precision|" + pct(truePos, truePos + falsePos)
                + "|false flags on clean|" + falsePos + " of " + clean
                + "|alerts per 1,000 records|" + String.format("%.1f", 1000.0 * flagged / rows.size()));

        // The data is fixed, so these are exact. They pin the numbers published in docs/EVALUATION.md.
        assertEquals(42, anomalies);
        assertEquals(42, truePos, "every injected anomaly must be flagged");
        assertEquals(0, falsePos, "no clean record may be flagged");
    }

    private static List<String[]> readCsv(String resource) throws IOException {
        List<String[]> out = new ArrayList<>();
        try (InputStream in = OfficialDataEvaluationTest.class.getResourceAsStream(resource);
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            br.readLine();                                  // header
            for (String line; (line = br.readLine()) != null; ) if (!line.isBlank()) out.add(line.split(",", -1));
        }
        return out;
    }

    private static String pct(int a, int b) {
        return b == 0 ? "n/a" : String.format("%.1f%%", 100.0 * a / b);
    }
}

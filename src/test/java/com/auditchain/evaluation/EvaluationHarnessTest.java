package com.auditchain.evaluation;

import com.auditchain.ai.MockVisionProvider;
import com.auditchain.core.HashChainEngine;
import com.auditchain.model.AuditEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproducible evaluation (seeded). Prints lines prefixed "EVAL|" so results can be copied from the CI log
 * into docs/EVALUATION.md. All data is synthetic.
 *
 * Part A: arithmetic-consistency screening of 200 synthetic result sheets.
 * Part B: tamper detection over 200 trials, split by attacker capability.
 */
class EvaluationHarnessTest {

    private static final String[] PARTIES = {"APC", "PDP", "LP", "NNPP"};
    private static final String[] TOTAL_KEYS = {"votes_cast", "total_votes", "totalvotes", "totalVotes"};
    private static final String GENESIS = "0".repeat(64);

    // ---------------------------------------------------------------- Part A

    @Test
    void partA_arithmeticScreening() {
        Random rnd = new Random(2026);
        MockVisionProvider provider = new MockVisionProvider();

        int clean = 0, cleanFlagged = 0;
        int mismatch = 0, mismatchFlagged = 0, offByOne = 0, offByOneFlagged = 0;
        int undeclared = 0, undeclaredFlagged = 0;
        int blank = 0, blankFlagged = 0;

        for (int i = 0; i < 200; i++) {
            int[] votes = new int[PARTIES.length];
            int sum = 0;
            for (int p = 0; p < votes.length; p++) { votes[p] = rnd.nextInt(400); sum += votes[p]; }
            String key = TOTAL_KEYS[rnd.nextInt(TOTAL_KEYS.length)];
            String parties = partyJson(votes);

            if (i < 100) {                       // 100 clean sheets
                clean++;
                if (provider.analyse(null, "{\"" + key + "\":" + sum + "," + parties + "}").isRequiresHumanReview()) cleanFlagged++;
            } else if (i < 160) {                // 60 altered totals (first 20 are off by exactly one)
                mismatch++;
                boolean one = i < 120;
                int delta = one ? 1 : 2 + rnd.nextInt(60);
                int declared = rnd.nextBoolean() ? sum + delta : Math.max(0, sum - delta);
                if (declared == sum) declared = sum + delta;
                boolean flagged = provider.analyse(null, "{\"" + key + "\":" + declared + "," + parties + "}").isRequiresHumanReview();
                if (flagged) mismatchFlagged++;
                if (one) { offByOne++; if (flagged) offByOneFlagged++; }
            } else if (i < 180) {                // 20 sheets with no declared total
                undeclared++;
                if (provider.analyse(null, "{" + parties + "}").isRequiresHumanReview()) undeclaredFlagged++;
            } else {                             // 20 blank payloads
                blank++;
                if (provider.analyse(null, i % 2 == 0 ? "" : "   ").isRequiresHumanReview()) blankFlagged++;
            }
        }

        System.out.println("EVAL|A|clean sheets|" + clean + "|flagged for review|" + cleanFlagged + "|false-flag rate|" + pct(cleanFlagged, clean));
        System.out.println("EVAL|A|altered totals|" + mismatch + "|flagged|" + mismatchFlagged + "|detection rate|" + pct(mismatchFlagged, mismatch));
        System.out.println("EVAL|A|of which off-by-one|" + offByOne + "|flagged|" + offByOneFlagged + "|detection rate|" + pct(offByOneFlagged, offByOne));
        System.out.println("EVAL|A|no declared total|" + undeclared + "|flagged|" + undeclaredFlagged + "|(known limit: nothing to compare against)");
        System.out.println("EVAL|A|blank payload|" + blank + "|flagged|" + blankFlagged + "|rate|" + pct(blankFlagged, blank));

        assertEquals(0, cleanFlagged, "clean sheets must not be flagged");
        assertEquals(mismatch, mismatchFlagged, "every altered total must be flagged");
        assertEquals(blank, blankFlagged, "blank payloads must go to a human");
    }

    // ---------------------------------------------------------------- Part B

    @Test
    void partB_tamperDetection() {
        Random rnd = new Random(7);
        int naive = 0, naiveDetected = 0;
        int tipRewrite = 0, tipRewriteChainDetected = 0, tipRewriteAnchorDetected = 0;
        int fullRewrite = 0, fullRewriteChainDetected = 0, fullRewriteAnchorDetected = 0;

        for (int trial = 0; trial < 200; trial++) {
            HashChainEngine engine = buildChain(30, rnd);
            String anchoredTip = engine.getLatestHash();   // what a signed checkpoint would bind
            List<AuditEntry> chain = engine.getChain();     // same entry objects as the live chain

            if (trial < 100) {                              // naive edit, no hash recompute
                naive++;
                AuditEntry e = chain.get(rnd.nextInt(chain.size()));
                switch (rnd.nextInt(4)) {
                    case 0 -> e.setPayload(e.getPayload() + " ");
                    case 1 -> e.setActorId("attacker");
                    case 2 -> e.setLocationCode("PU-9999");
                    default -> e.setPreviousHash("f".repeat(64));
                }
                if (engine.verifyChain().isPresent()) naiveDetected++;
            } else if (trial < 150) {                       // rewrite newest entry and recompute its own hash
                tipRewrite++;
                AuditEntry tip = chain.get(chain.size() - 1);
                tip.setPayload("{\"forged\":true}");
                tip.setCurrentHash(engine.computeHash(tip.toCanonicalString()));
                if (engine.verifyChain().isPresent()) tipRewriteChainDetected++;
                if (!anchoredTip.equals(engine.getLatestHash())) tipRewriteAnchorDetected++;
            } else {                                        // rewrite an entry and recompute every later hash
                fullRewrite++;
                int at = rnd.nextInt(chain.size());
                chain.get(at).setPayload("{\"forged\":true}");
                for (int i = at; i < chain.size(); i++) {
                    AuditEntry e = chain.get(i);
                    e.setPreviousHash(i == 0 ? GENESIS : chain.get(i - 1).getCurrentHash());
                    e.setCurrentHash(engine.computeHash(e.toCanonicalString()));
                }
                if (engine.verifyChain().isPresent()) fullRewriteChainDetected++;
                if (!anchoredTip.equals(engine.getLatestHash())) fullRewriteAnchorDetected++;
            }
        }

        System.out.println("EVAL|B|naive edits|" + naive + "|detected by chain|" + naiveDetected + "|rate|" + pct(naiveDetected, naive));
        System.out.println("EVAL|B|newest-entry rewrite with hash recompute|" + tipRewrite + "|detected by chain alone|" + tipRewriteChainDetected + "|detected against anchored tip|" + tipRewriteAnchorDetected);
        System.out.println("EVAL|B|full downstream rewrite with recompute|" + fullRewrite + "|detected by chain alone|" + fullRewriteChainDetected + "|detected against anchored tip|" + fullRewriteAnchorDetected);

        assertEquals(naive, naiveDetected);
        assertEquals(0, tipRewriteChainDetected, "chain alone cannot see a consistent rewrite; anchors exist for this");
        assertEquals(tipRewrite, tipRewriteAnchorDetected);
        assertEquals(0, fullRewriteChainDetected);
        assertEquals(fullRewrite, fullRewriteAnchorDetected);
        assertTrue(true);
    }

    // ---------------------------------------------------------------- helpers

    private static HashChainEngine buildChain(int n, Random rnd) {
        HashChainEngine engine = new HashChainEngine();
        for (int i = 0; i < n; i++) {
            int[] v = new int[PARTIES.length];
            int sum = 0;
            for (int p = 0; p < v.length; p++) { v[p] = rnd.nextInt(400); sum += v[p]; }
            engine.append("{\"votes_cast\":" + sum + "," + partyJson(v) + "}",
                    AuditEntry.ActorRole.PU_OFFICER, "officer-" + i, "PU-" + (1000 + i));
        }
        return engine;
    }

    private static String partyJson(int[] votes) {
        StringBuilder sb = new StringBuilder("\"party_results\":{");
        for (int p = 0; p < votes.length; p++) {
            if (p > 0) sb.append(',');
            sb.append('"').append(PARTIES[p]).append("\":").append(votes[p]);
        }
        return sb.append('}').toString();
    }

    private static String pct(int a, int b) {
        return b == 0 ? "n/a" : String.format("%.1f%%", 100.0 * a / b);
    }
}

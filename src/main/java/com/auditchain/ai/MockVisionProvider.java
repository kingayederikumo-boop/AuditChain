package com.auditchain.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic mock for automated tests and demos.
 * Parses simple JSON-like declared payload; flags arithmetic inconsistency only.
 * NEVER emits a fraud declaration.
 */
@Component
public class MockVisionProvider implements VisionProvider {

    private static final Pattern PARTY = Pattern.compile("\"([A-Z]{2,5})\"\\s*:\\s*(\\d+)");

    @Override
    public VisionResult analyse(byte[] imageBytes, String declaredPayload) {
        VisionResult r = new VisionResult();
        r.setNotes("mock-provider; no real vision model invoked");
        if (declaredPayload == null || declaredPayload.isBlank()) {
            r.setRequiresHumanReview(true);
            r.setArithmeticConsistent(false);
            r.setNotes("no declared payload; human review required");
            return r;
        }
        int sum = 0;
        Matcher m = PARTY.matcher(declaredPayload);
        while (m.find()) {
            int v = Integer.parseInt(m.group(2));
            r.getPartyVotes().put(m.group(1), v);
            sum += v;
        }
        r.setTotalVotesExtracted(sum);

        int declaredTotal = -1;
        Matcher t = Pattern.compile("\"(?:votes_cast|total_votes)\"\\s*:\\s*(\\d+)").matcher(declaredPayload);
        if (t.find()) declaredTotal = Integer.parseInt(t.group(1));

        boolean consistent = declaredTotal < 0 || declaredTotal == sum;
        r.setArithmeticConsistent(consistent);
        r.setRequiresHumanReview(!consistent);
        if (!consistent) {
            r.setNotes("party sum " + sum + " != declared total " + declaredTotal + "; queued for human review");
        }
        return r;
    }
}

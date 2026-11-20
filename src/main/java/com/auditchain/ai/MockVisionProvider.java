package com.auditchain.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    private static final Pattern TOTAL =
            Pattern.compile("\"(?:votes_cast|total_votes|totalvotes)\"\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern EXTRA =
            Pattern.compile("\"(registered_voters|accredited_voters|rejected_votes)\"\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

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
        Matcher t = TOTAL.matcher(declaredPayload);
        if (t.find()) declaredTotal = Integer.parseInt(t.group(1));

        boolean consistent = declaredTotal < 0 || declaredTotal == sum;

        // Optional extra figures: registered, accredited, rejected. Rules that need a missing figure are skipped.
        Integer registered = null, accredited = null, rejected = null;
        Matcher x = EXTRA.matcher(declaredPayload);
        while (x.find()) {
            int v = Integer.parseInt(x.group(2));
            switch (x.group(1).toLowerCase()) {
                case "registered_voters" -> registered = v;
                case "accredited_voters" -> accredited = v;
                default -> rejected = v;
            }
        }
        int[] votes = r.getPartyVotes().values().stream().mapToInt(Integer::intValue).toArray();
        List<String> reasons = new ArrayList<>(SheetRules.check(registered, accredited, rejected, votes));

        boolean flagged = !consistent || !reasons.isEmpty();
        r.setArithmeticConsistent(consistent && !reasons.contains(SheetRules.VOTES_DO_NOT_MATCH_ACCREDITED));
        r.setRequiresHumanReview(flagged);
        if (flagged) {
            StringBuilder n = new StringBuilder();
            if (!consistent) n.append("party sum ").append(sum).append(" != declared total ").append(declaredTotal).append("; ");
            if (!reasons.isEmpty()) n.append("rules fired: ").append(String.join(", ", reasons)).append("; ");
            r.setNotes(n.append("queued for human review").toString());
        }
        return r;
    }
}

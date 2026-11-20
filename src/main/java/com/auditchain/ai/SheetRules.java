package com.auditchain.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based consistency checks on the figures of one result sheet.
 *
 * These rules only look at arithmetic and plausibility. A flag means "a person should look at this sheet".
 * It is never evidence of wrongdoing, and nothing here declares fraud or decides a result.
 *
 * Every figure is optional (null when the sheet does not carry it). A rule that needs a missing figure is skipped.
 */
public final class SheetRules {

    public static final String ACCREDITED_EXCEEDS_REGISTERED = "accredited_exceeds_registered";
    public static final String VOTES_DO_NOT_MATCH_ACCREDITED = "votes_do_not_match_accredited";
    public static final String ROUND_NUMBERS = "round_numbers";
    public static final String SAME_AS_PREVIOUS = "same_as_previous_unit";

    private SheetRules() {}

    /**
     * Single-sheet rules.
     *
     * @param registered registered voters, or null
     * @param accredited accredited voters, or null
     * @param rejected   rejected ballots, or null
     * @param partyVotes valid votes per party, or null/empty when unknown
     * @return reason codes that fired; empty when nothing needs a second look
     */
    public static List<String> check(Integer registered, Integer accredited, Integer rejected, int[] partyVotes) {
        List<String> out = new ArrayList<>();
        boolean haveVotes = partyVotes != null && partyVotes.length > 0;
        int valid = 0;
        if (haveVotes) for (int v : partyVotes) valid += v;

        if (registered != null && accredited != null && accredited > registered) {
            out.add(ACCREDITED_EXCEEDS_REGISTERED);
        }
        if (haveVotes && accredited != null && rejected != null && valid + rejected != accredited) {
            out.add(VOTES_DO_NOT_MATCH_ACCREDITED);
        }
        if (haveVotes && valid > 0 && allMultiplesOf100(partyVotes)) {
            out.add(ROUND_NUMBERS);
        }
        return out;
    }

    /** Batch rule: identical party figures to the unit listed just before it. Only meaningful for an ordered list. */
    public static boolean sameAsPrevious(int[] previous, int[] current) {
        if (previous == null || current == null || previous.length != current.length || current.length == 0) return false;
        for (int i = 0; i < current.length; i++) if (previous[i] != current[i]) return false;
        return true;
    }

    private static boolean allMultiplesOf100(int[] votes) {
        for (int v : votes) if (v % 100 != 0) return false;
        return true;
    }
}

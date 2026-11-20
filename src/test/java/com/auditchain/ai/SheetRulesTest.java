package com.auditchain.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SheetRulesTest {

    @Test
    void cleanSheetFiresNothing() {
        assertTrue(SheetRules.check(1000, 500, 10, new int[]{200, 150, 100, 30, 10}).isEmpty());
    }

    @Test
    void accreditedAboveRegisteredIsFlagged() {
        List<String> r = SheetRules.check(400, 500, 10, new int[]{200, 150, 100, 30, 10});
        assertTrue(r.contains(SheetRules.ACCREDITED_EXCEEDS_REGISTERED));
    }

    @Test
    void validPlusRejectedMustEqualAccredited() {
        List<String> r = SheetRules.check(1000, 500, 10, new int[]{200, 150, 100, 30, 11});
        assertEquals(List.of(SheetRules.VOTES_DO_NOT_MATCH_ACCREDITED), r);
    }

    @Test
    void roundNumbersAreFlaggedButAllZeroIsNot() {
        assertTrue(SheetRules.check(1000, 500, 0, new int[]{100, 200, 100, 100, 0}).contains(SheetRules.ROUND_NUMBERS));
        assertFalse(SheetRules.check(1000, 0, 0, new int[]{0, 0, 0, 0, 0}).contains(SheetRules.ROUND_NUMBERS));
    }

    @Test
    void missingFiguresSkipTheRulesThatNeedThem() {
        assertTrue(SheetRules.check(null, null, null, new int[]{3, 51, 186, 31, 279}).isEmpty());
        assertTrue(SheetRules.check(null, 500, null, new int[]{3, 51, 186, 31, 279}).isEmpty());
    }

    @Test
    void sameAsPreviousNeedsIdenticalFigures() {
        assertTrue(SheetRules.sameAsPrevious(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
        assertFalse(SheetRules.sameAsPrevious(new int[]{1, 2, 3}, new int[]{1, 2, 4}));
        assertFalse(SheetRules.sameAsPrevious(null, new int[]{1, 2, 3}));
    }

    @Test
    void mockProviderAppliesTheRulesWhenTheFiguresArePresent() {
        MockVisionProvider p = new MockVisionProvider();
        String bad = "{\"registered_voters\":1000,\"accredited_voters\":500,\"rejected_votes\":10,"
                + "\"party_results\":{\"APC\":200,\"PDP\":150,\"LP\":100,\"NNPP\":30,\"ADC\":11}}";
        var r = p.analyse(null, bad);
        assertTrue(r.isRequiresHumanReview());
        assertTrue(r.getNotes().contains(SheetRules.VOTES_DO_NOT_MATCH_ACCREDITED));
        assertFalse(r.getNotes().toLowerCase().contains("fraud"));

        String good = bad.replace("\"ADC\":11", "\"ADC\":10");
        assertFalse(p.analyse(null, good).isRequiresHumanReview());
    }
}

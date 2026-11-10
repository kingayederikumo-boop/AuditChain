package com.auditchain.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockVisionProviderTest {
    private final MockVisionProvider provider = new MockVisionProvider();

    @Test
    void consistentPayloadPasses() {
        String json = "{\"votes_cast\":100,\"party_results\":{\"APC\":60,\"PDP\":40}}";
        var r = provider.analyse(null, json);
        assertTrue(r.isArithmeticConsistent());
        assertFalse(r.isRequiresHumanReview());
        assertEquals(100, r.getTotalVotesExtracted());
    }

    @Test
    void inconsistentPayloadFlagsReview() {
        String json = "{\"votes_cast\":100,\"party_results\":{\"APC\":60,\"PDP\":50}}";
        var r = provider.analyse(null, json);
        assertFalse(r.isArithmeticConsistent());
        assertTrue(r.isRequiresHumanReview());
        assertFalse(r.getNotes().toLowerCase().contains("fraud"));
    }

    @Test
    void allTotalKeySpellingsAreChecked() {
        for (String key : new String[]{"votes_cast", "total_votes", "totalvotes", "totalVotes", "TOTAL_VOTES"}) {
            String bad = "{\"" + key + "\":100,\"party_results\":{\"APC\":60,\"PDP\":50}}";
            var r = provider.analyse(null, bad);
            assertFalse(r.isArithmeticConsistent(), key + " mismatch must be flagged");
            assertTrue(r.isRequiresHumanReview(), key);

            String good = "{\"" + key + "\":110,\"party_results\":{\"APC\":60,\"PDP\":50}}";
            var ok = provider.analyse(null, good);
            assertTrue(ok.isArithmeticConsistent(), key + " match must pass");
            assertFalse(ok.isRequiresHumanReview(), key);
        }
    }

    @Test
    void blankPayloadRequiresReview() {
        var r = provider.analyse(null, " ");
        assertTrue(r.isRequiresHumanReview());
    }
}

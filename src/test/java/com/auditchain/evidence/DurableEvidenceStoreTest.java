package com.auditchain.evidence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DurableEvidenceStoreTest {
    @TempDir Path temp;

    @Test
    void idempotencySurvivesReopen() {
        Path db = temp.resolve("evidence.db");
        DurableEvidenceStore first = new DurableEvidenceStore(db);
        EvidenceSubmission sub = new EvidenceSubmission();
        sub.setIdempotencyKey("key-1");
        sub.setActorId("officer-1");
        sub.setEvidenceFingerprint("abc");
        sub.setReceiptHash("hash1");
        sub.setChainEntryId(UUID.randomUUID());
        first.save(sub);
        first.close();

        DurableEvidenceStore second = new DurableEvidenceStore(db);
        var found = second.findByIdempotency("key-1");
        assertTrue(found.isPresent());
        assertEquals("abc", found.get().getEvidenceFingerprint());
        assertEquals("officer-1", found.get().getActorId());
        second.close();
    }

    @Test
    void reviewDecisionSurvivesReopen() {
        Path db = temp.resolve("reviews.db");
        DurableEvidenceStore first = new DurableEvidenceStore(db);
        first.saveReview("review-1", "evidence-1", "counts require review",
                false, true, "PENDING", Instant.parse("2026-10-29T10:00:00Z").toString());
        first.updateReviewDecision("review-1", "NEEDS_MORE_INFO", "reviewer-1",
                "please attach a clearer scan", Instant.parse("2026-10-29T10:05:00Z").toString());
        first.close();

        DurableEvidenceStore reopened = new DurableEvidenceStore(db);
        try {
            var rows = reopened.loadReviews();
            assertEquals(1, rows.size());
            assertEquals("NEEDS_MORE_INFO", rows.get(0).get("decision"));
            assertEquals("reviewer-1", rows.get(0).get("reviewerId"));
            assertEquals("please attach a clearer scan", rows.get(0).get("reviewerNotes"));
            assertEquals("2026-10-29T10:05:00Z", rows.get(0).get("decidedAt"));
        } finally {
            reopened.close();
        }
    }
}

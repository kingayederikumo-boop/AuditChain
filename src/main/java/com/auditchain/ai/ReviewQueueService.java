package com.auditchain.ai;

import com.auditchain.evidence.DurableEvidenceStore;
import com.auditchain.evidence.EvidenceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ReviewQueueService {

    public enum Decision { PENDING, CONSISTENT, INCONSISTENT, NEEDS_MORE_INFO }

    public static final class ReviewItem {
        public final UUID id = UUID.randomUUID();
        public final String evidenceId;
        public final VisionResult visionResult;
        public final Instant enqueuedAt = Instant.now();
        public volatile Decision decision = Decision.PENDING;
        public String reviewerId;
        public Instant decidedAt;
        public String reviewerNotes;

        public ReviewItem(String evidenceId, VisionResult visionResult) {
            this.evidenceId = evidenceId;
            this.visionResult = visionResult;
        }
    }

    private final Queue<ReviewItem> pending = new ConcurrentLinkedQueue<>();
    private final Map<UUID, ReviewItem> byId = new ConcurrentHashMap<>();
    private final DurableEvidenceStore store;

    public ReviewQueueService(EvidenceService evidenceService) {
        this.store = evidenceService.store();
    }

    public ReviewItem enqueue(String evidenceId, VisionResult result) {
        ReviewItem item = new ReviewItem(evidenceId, result);
        pending.add(item);
        byId.put(item.id, item);
        store.saveReview(
                item.id.toString(),
                evidenceId,
                result.getNotes(),
                result.isArithmeticConsistent(),
                result.isRequiresHumanReview(),
                Decision.PENDING.name(),
                item.enqueuedAt.toString()
        );
        return item;
    }

    public List<ReviewItem> listPending() {
        return pending.stream().filter(i -> i.decision == Decision.PENDING).toList();
    }

    public Optional<ReviewItem> decide(UUID itemId, String reviewerId, Decision decision, String notes) {
        ReviewItem item = byId.get(itemId);
        if (item == null) return Optional.empty();
        if (item.decision != Decision.PENDING) {
            throw new IllegalStateException("already decided");
        }
        item.decision = decision;
        item.reviewerId = reviewerId;
        item.decidedAt = Instant.now();
        item.reviewerNotes = notes;
        pending.remove(item);
        store.updateReviewDecision(
                item.id.toString(),
                decision.name(),
                reviewerId,
                notes,
                item.decidedAt.toString()
        );
        return Optional.of(item);
    }
}

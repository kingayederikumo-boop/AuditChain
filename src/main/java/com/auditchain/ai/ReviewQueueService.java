package com.auditchain.ai;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Human-review queue. AI only flags inconsistencies; humans decide.
 * Decisions are auditable (reviewer id + timestamp + notes).
 * AI must never independently declare electoral fraud.
 */
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

    public ReviewItem enqueue(String evidenceId, VisionResult result) {
        ReviewItem item = new ReviewItem(evidenceId, result);
        pending.add(item);
        byId.put(item.id, item);
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
        return Optional.of(item);
    }
}

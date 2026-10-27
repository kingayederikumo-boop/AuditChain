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
        public final UUID id;
        public final String evidenceId;
        public final VisionResult visionResult;
        public final Instant enqueuedAt;
        public volatile Decision decision = Decision.PENDING;
        public String reviewerId;
        public Instant decidedAt;
        public String reviewerNotes;

        public ReviewItem(String evidenceId, VisionResult visionResult) {
            this(UUID.randomUUID(), evidenceId, visionResult, Instant.now());
        }

        public ReviewItem(UUID id, String evidenceId, VisionResult visionResult, Instant enqueuedAt) {
            this.id = id;
            this.evidenceId = evidenceId;
            this.visionResult = visionResult;
            this.enqueuedAt = enqueuedAt;
        }
    }

    private final Queue<ReviewItem> pending = new ConcurrentLinkedQueue<>();
    private final Map<UUID, ReviewItem> byId = new ConcurrentHashMap<>();
    private final DurableEvidenceStore store;

    public ReviewQueueService(EvidenceService evidenceService) {
        this.store = evidenceService.store();
        for (Map<String, Object> row : store.loadReviews()) {
            VisionResult vision = new VisionResult();
            vision.setNotes((String) row.get("notes"));
            vision.setArithmeticConsistent(Boolean.TRUE.equals(row.get("arithmeticConsistent")));
            vision.setRequiresHumanReview(Boolean.TRUE.equals(row.get("requiresHumanReview")));
            ReviewItem item = new ReviewItem(UUID.fromString((String) row.get("id")),
                    (String) row.get("evidenceId"), vision, Instant.parse((String) row.get("enqueuedAt")));
            item.decision = Decision.valueOf((String) row.get("decision"));
            item.reviewerId = (String) row.get("reviewerId");
            item.reviewerNotes = (String) row.get("reviewerNotes");
            Object decidedAt = row.get("decidedAt");
            if (decidedAt instanceof String value && !value.isBlank()) item.decidedAt = Instant.parse(value);
            byId.put(item.id, item);
            if (item.decision == Decision.PENDING) pending.add(item);
        }
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

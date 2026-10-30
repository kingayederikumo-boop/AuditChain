package com.auditchain.ai;

import com.auditchain.evidence.DurableEvidenceStore;
import com.auditchain.evidence.EvidenceService;
import com.auditchain.service.ChainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ChainService chainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewQueueService(EvidenceService evidenceService, ChainService chainService) {
        this.store = evidenceService.store();
        this.chainService = chainService;
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

    /** Record every AI result, including clean results that do not need human review. */
    public synchronized void recordAiResult(String evidenceId, VisionResult result) {
        ObjectNode aiEvent = objectMapper.createObjectNode();
        aiEvent.put("type", "AI_CHECK");
        aiEvent.put("evidenceId", evidenceId);
        aiEvent.put("arithmeticConsistent", result.isArithmeticConsistent());
        aiEvent.put("requiresHumanReview", result.isRequiresHumanReview());
        aiEvent.put("notes", result.getNotes() == null ? "" : result.getNotes());
        try {
            chainService.appendSystemEvent(aiEvent.toString(), "system:vision", null);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to record AI result on audit chain", ex);
        }
    }

    public synchronized ReviewItem enqueue(String evidenceId, VisionResult result) {
        recordAiResult(evidenceId, result);
        return enqueueAfterAiRecorded(evidenceId, result);
    }

    /** Queue a result whose AI_CHECK event has already been appended. */
    public synchronized ReviewItem enqueueAfterAiRecorded(String evidenceId, VisionResult result) {
        ReviewItem item = new ReviewItem(evidenceId, result);
        store.saveReview(
                item.id.toString(),
                evidenceId,
                result.getNotes(),
                result.isArithmeticConsistent(),
                result.isRequiresHumanReview(),
                Decision.PENDING.name(),
                item.enqueuedAt.toString()
        );
        pending.add(item);
        byId.put(item.id, item);
        return item;
    }

    public List<ReviewItem> listPending() {
        return pending.stream().filter(i -> i.decision == Decision.PENDING).toList();
    }

    public synchronized Optional<ReviewItem> decide(UUID itemId, String reviewerId, Decision decision, String notes) {
        ReviewItem item = byId.get(itemId);
        if (item == null) return Optional.empty();
        if (item.decision != Decision.PENDING) {
            throw new IllegalStateException("already decided");
        }
        Instant decidedAt = Instant.now();
        ObjectNode decisionEvent = objectMapper.createObjectNode();
        decisionEvent.put("type", "REVIEW_DECISION");
        decisionEvent.put("reviewItemId", item.id.toString());
        decisionEvent.put("evidenceId", item.evidenceId);
        decisionEvent.put("decision", decision.name());
        decisionEvent.put("reviewerId", reviewerId == null ? "" : reviewerId);
        decisionEvent.put("notes", notes == null ? "" : notes);
        decisionEvent.put("decidedAt", decidedAt.toString());
        try {
            chainService.appendSystemEvent(decisionEvent.toString(), "review:" + (reviewerId == null ? "unknown" : reviewerId), null);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to record reviewer decision on audit chain", ex);
        }
        store.updateReviewDecision(item.id.toString(), decision.name(), reviewerId, notes, decidedAt.toString());
        item.decision = decision;
        item.reviewerId = reviewerId;
        item.decidedAt = decidedAt;
        item.reviewerNotes = notes;
        pending.remove(item);
        return Optional.of(item);
    }
}

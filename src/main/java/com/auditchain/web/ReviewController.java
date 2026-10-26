package com.auditchain.web;

import com.auditchain.ai.ReviewQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewQueueService reviewQueue;

    public ReviewController(ReviewQueueService reviewQueue) {
        this.reviewQueue = reviewQueue;
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> pending() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var item : reviewQueue.listPending()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", item.id.toString());
            m.put("evidenceId", item.evidenceId);
            m.put("enqueuedAt", item.enqueuedAt.toString());
            m.put("notes", item.visionResult.getNotes());
            m.put("arithmeticConsistent", item.visionResult.isArithmeticConsistent());
            out.add(m);
        }
        return out;
    }

    @PostMapping("/{id}/decide")
    public ResponseEntity<?> decide(@PathVariable UUID id,
                                    @RequestParam String reviewerId,
                                    @RequestParam String decision,
                                    @RequestParam(required = false) String notes) {
        ReviewQueueService.Decision d;
        try {
            d = ReviewQueueService.Decision.valueOf(decision.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid decision"));
        }
        if (d == ReviewQueueService.Decision.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "cannot set PENDING"));
        }
        return reviewQueue.decide(id, reviewerId, d, notes)
                .map(item -> ResponseEntity.ok(Map.of(
                        "id", item.id.toString(),
                        "decision", item.decision.name(),
                        "reviewerId", item.reviewerId,
                        "decidedAt", item.decidedAt.toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}

package com.auditchain.web;

import com.auditchain.ai.ReviewQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

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
                                    @RequestParam(required = false) String notes,
                                    HttpServletRequest request) {
        ReviewQueueService.Decision d;
        try {
            d = ReviewQueueService.Decision.valueOf(decision.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid decision"));
        }
        if (d == ReviewQueueService.Decision.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "cannot set PENDING"));
        }
        String authenticatedReviewer = (String) request.getAttribute("auditchain.actorId");
        if (authenticatedReviewer == null) {
            return ResponseEntity.status(401).body(Map.of("error", "authenticated reviewer identity unavailable"));
        }
        return reviewQueue.decide(id, authenticatedReviewer, d, notes)
                .map(item -> ResponseEntity.ok(Map.of(
                        "id", item.id.toString(),
                        "decision", item.decision.name(),
                        "reviewerId", item.reviewerId,
                        "decidedAt", item.decidedAt.toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}

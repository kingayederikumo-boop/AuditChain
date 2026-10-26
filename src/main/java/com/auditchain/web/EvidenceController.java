package com.auditchain.web;

import com.auditchain.ai.MockVisionProvider;
import com.auditchain.ai.ReviewQueueService;
import com.auditchain.ai.VisionResult;
import com.auditchain.evidence.EvidenceService;
import com.auditchain.evidence.EvidenceSubmission;
import com.auditchain.security.SimpleRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final MockVisionProvider visionProvider;
    private final ReviewQueueService reviewQueue;
    private final SimpleRateLimiter rateLimiter;

    public EvidenceController(EvidenceService evidenceService,
                              MockVisionProvider visionProvider,
                              ReviewQueueService reviewQueue,
                              SimpleRateLimiter rateLimiter) {
        this.evidenceService = evidenceService;
        this.visionProvider = visionProvider;
        this.reviewQueue = reviewQueue;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<?> submit(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestParam String actorId,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String payloadJson,
            @RequestParam(required = false) MultipartFile file) throws Exception {

        String key = idempotencyKey != null ? idempotencyKey : "auto-" + actorId + "-" + System.currentTimeMillis();
        if (!rateLimiter.tryAcquire(actorId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "rate limit exceeded"));
        }

        byte[] bytes = file != null && !file.isEmpty() ? file.getBytes() : null;
        String contentType = file != null ? file.getContentType() : "application/json";

        EvidenceSubmission sub = evidenceService.submit(key, actorId, locationCode, contentType, bytes, payloadJson);

        VisionResult vision = visionProvider.analyse(bytes, payloadJson);
        if (vision.isRequiresHumanReview()) {
            reviewQueue.enqueue(sub.getId().toString(), vision);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("submissionId", sub.getId().toString());
        body.put("idempotencyKey", sub.getIdempotencyKey());
        body.put("fingerprint", sub.getEvidenceFingerprint());
        body.put("receiptHash", sub.getReceiptHash());
        body.put("chainEntryId", sub.getChainEntryId().toString());
        body.put("vision", Map.of(
                "arithmeticConsistent", vision.isArithmeticConsistent(),
                "requiresHumanReview", vision.isRequiresHumanReview(),
                "notes", vision.getNotes() == null ? "" : vision.getNotes(),
                "partyVotes", vision.getPartyVotes()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}

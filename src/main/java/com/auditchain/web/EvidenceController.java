package com.auditchain.web;

import com.auditchain.ai.ReviewQueueService;
import com.auditchain.ai.VisionProvider;
import com.auditchain.ai.VisionResult;
import com.auditchain.evidence.EvidenceService;
import com.auditchain.evidence.EvidenceConflictException;
import com.auditchain.evidence.EvidenceSubmission;
import com.auditchain.security.SimpleRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final VisionProvider visionProvider;
    private final ReviewQueueService reviewQueue;
    private final SimpleRateLimiter rateLimiter;

    public EvidenceController(EvidenceService evidenceService,
                              VisionProvider visionProvider,
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
            @RequestParam(required = false) MultipartFile file,
            HttpServletRequest request) throws Exception {

        if (idempotencyKey == null || idempotencyKey.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Idempotency-Key required"));
        String key = idempotencyKey;
        String authenticatedActor = (String) request.getAttribute("auditchain.actorId");
        String rateLimitKey = (String) request.getAttribute("auditchain.rateLimitKey");
        if (authenticatedActor == null || rateLimitKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "authenticated actor identity unavailable"));
        }
        if (!rateLimiter.tryAcquire(rateLimitKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "rate limit exceeded"));
        }

        if (file != null && file.getSize() > 2L * 1024 * 1024)
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", "Image exceeds 2 MB"));
        byte[] bytes = file != null && !file.isEmpty() ? file.getBytes() : null;
        String contentType = file != null ? file.getContentType() : "application/json";

        boolean previouslySubmitted = evidenceService.findByIdempotency(key).isPresent();
        EvidenceSubmission sub;
        try {
            sub = evidenceService.submit(key, authenticatedActor, locationCode, contentType, bytes, payloadJson);
        } catch (EvidenceConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }

        VisionResult vision = visionProvider.analyse(bytes, payloadJson);
        if (!previouslySubmitted && vision.isRequiresHumanReview()) {
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
        return ResponseEntity.status(previouslySubmitted ? HttpStatus.OK : HttpStatus.CREATED).body(body);
    }
}

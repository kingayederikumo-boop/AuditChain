package com.auditchain.web;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.crypto.ExternalAnchorVerifier;
import com.auditchain.security.SimpleRateLimiter;
import com.auditchain.service.ChainService;
import com.auditchain.model.AuditEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    private static final long VERIFY_TTL_OK_MS = 30_000;
    private static final long VERIFY_TTL_FAIL_MS = 5_000;

    private final CheckpointService checkpointService;
    private final ExternalAnchorVerifier externalAnchorVerifier;
    private final ChainService chainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** /verify performs an outbound fetch, so it is capped per client and globally to prevent abuse. */
    private final SimpleRateLimiter verifyPerClient = new SimpleRateLimiter(20);
    private final SimpleRateLimiter verifyGlobal = new SimpleRateLimiter(120);

    /**
     * Result of the last full verification. It is reused only while the chain, its version counter and the
     * checkpoint list are unchanged, and only for a short time, so any change to the chain is verified afresh.
     */
    private volatile CachedVerify cachedVerify;

    private record CachedVerify(String key, long atMs, long ttlMs, Map<String, Object> result) {}

    public CheckpointController(CheckpointService checkpointService,
                                ChainService chainService,
                                ExternalAnchorVerifier externalAnchorVerifier) {
        this.checkpointService = checkpointService;
        this.chainService = chainService;
        this.externalAnchorVerifier = externalAnchorVerifier;
        restoreFromChain();
    }

    private void restoreFromChain() {
        for (AuditEntry entry : chainService.getChain()) {
            try {
                JsonNode node = objectMapper.readTree(entry.getPayload());
                if (!"CHECKPOINT".equals(node.path("type").asText())) continue;
                checkpointService.restorePublished(new CheckpointService.Checkpoint(
                        node.path("sequence").asLong(),
                        node.path("chainTipHash").asText(),
                        Instant.parse(node.path("createdAt").asText()),
                        node.path("signatureHex").asText(),
                        node.path("publicKeyHex").asText()));
            } catch (Exception ignored) {
                // Non-checkpoint entries and legacy payloads are intentionally ignored.
            }
        }
    }

    @PostMapping
    public Map<String, Object> create() {
        var cp = checkpointService.createAndPublish(chainService.size(), chainService.getLatestHash());
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "CHECKPOINT");
        event.put("sequence", cp.sequence);
        event.put("chainTipHash", cp.chainTipHash);
        event.put("createdAt", cp.createdAt.toString());
        event.put("signatureHex", cp.signatureHex);
        event.put("publicKeyHex", cp.publicKeyHex);
        try {
            chainService.appendSystemEvent(objectMapper.writeValueAsString(event), "system:checkpoint", null);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to persist checkpoint in audit chain", ex);
        }
        return toView(cp);
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return checkpointService.listPublished().stream().map(this::toView).collect(Collectors.toList());
    }

    @GetMapping("/verify")
    public Map<String, Object> verifyPublished() {
        enforceVerifyLimits();

        String key = chainService.version() + ":" + chainService.size() + ":" + chainService.getLatestHash()
                + ":" + checkpointService.listPublished().size();
        long now = System.currentTimeMillis();
        CachedVerify cached = cachedVerify;
        if (cached != null && cached.key().equals(key) && now - cached.atMs() < cached.ttlMs()) {
            return cached.result();
        }

        Map<String, Object> result = computeVerification();
        boolean ok = Boolean.TRUE.equals(result.get("valid"));
        cachedVerify = new CachedVerify(key, now, ok ? VERIFY_TTL_OK_MS : VERIFY_TTL_FAIL_MS, result);
        return result;
    }

    private Map<String, Object> computeVerification() {
        var chain = chainService.getChain();
        Map<Long, String> hashes = chain.stream().collect(Collectors.toMap(
                entry -> entry.getSequenceNumber(), entry -> entry.getCurrentHash()));
        hashes.put(0L, "0000000000000000000000000000000000000000000000000000000000000000");

        var internalIssue = checkpointService.detectTampering(chainService.size(), chainService.getLatestHash(),
                hashes, checkpointService.listPublished());
        var external = externalAnchorVerifier.verify(chain, checkpointService);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", internalIssue.isEmpty() && external.valid());
        result.put("chainSize", chainService.size());
        result.put("checkpointCount", checkpointService.listPublished().size());
        result.put("internalCheckpointValid", internalIssue.isEmpty());
        result.put("internalCheckpointError", internalIssue.orElse(null));
        result.put("externalAnchorCount", external.checkpointCount());
        result.put("externalAnchorsValid", external.valid());
        result.put("error", internalIssue.orElse(external.error()));
        result.put("externalAnchorError", external.error());
        result.put("checkedAt", Instant.now().toString());
        return result;
    }

    private void enforceVerifyLimits() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servlet)) return; // not in an HTTP request (unit tests)
        HttpServletRequest req = servlet.getRequest();
        String client = req.getRemoteAddr();
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // The proxy appends the real peer last; earlier entries are client-supplied.
            String[] parts = forwarded.split(",");
            client = parts[parts.length - 1].trim();
        }
        if (!verifyGlobal.tryAcquire("global") || !verifyPerClient.tryAcquire(client)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "verification rate limit exceeded");
        }
    }

    @GetMapping("/public-key")
    public Map<String, String> publicKey() {
        return Map.of("publicKeyHex", checkpointService.publicKeyHex());
    }

    private Map<String, Object> toView(CheckpointService.Checkpoint cp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sequence", cp.sequence);
        m.put("chainTipHash", cp.chainTipHash);
        m.put("createdAt", cp.createdAt.toString());
        m.put("signatureHex", cp.signatureHex);
        m.put("publicKeyHex", cp.publicKeyHex);
        m.put("valid", checkpointService.verify(cp));
        return m;
    }
}

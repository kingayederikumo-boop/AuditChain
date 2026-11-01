package com.auditchain.web;

import com.auditchain.crypto.AnchorLogVerifier;
import com.auditchain.crypto.CheckpointService;
import com.auditchain.service.ChainService;
import com.auditchain.model.AuditEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    private static final long ANCHOR_CACHE_MILLIS = 60_000L;

    private final CheckpointService checkpointService;
    private final ChainService chainService;
    private final String anchorLogUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile String cachedAnchorLog;
    private volatile long cachedAnchorLogAtMillis;

    public CheckpointController(CheckpointService checkpointService, ChainService chainService,
                                @Value("${auditchain.anchors.log-url:https://raw.githubusercontent.com/kingayederikumo-boop/AuditChain/audit-anchors/anchors/log.jsonl}") String anchorLogUrl) {
        this.checkpointService = checkpointService;
        this.chainService = chainService;
        this.anchorLogUrl = anchorLogUrl;
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
        var chain = chainService.getChain();
        Map<Long, String> hashes = chain.stream().collect(Collectors.toMap(
                entry -> entry.getSequenceNumber(), entry -> entry.getCurrentHash()));
        var issue = checkpointService.detectTampering(chainService.size(), chainService.getLatestHash(),
                hashes, checkpointService.listPublished());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", issue.isEmpty());
        result.put("chainSize", chainService.size());
        result.put("checkpointCount", checkpointService.listPublished().size());
        result.put("error", issue.orElse(null));
        return result;
    }

    /**
     * Compares the live chain with the signed checkpoints published on the public anchor
     * log. Unlike /verify, this does not rely on checkpoints stored inside the chain, so
     * deleting CHECKPOINT entries or re-hashing history is still detected.
     */
    @GetMapping("/verify-anchors")
    public ResponseEntity<Map<String, Object>> verifyAnchors() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("source", anchorLogUrl);
        String log;
        try {
            log = fetchAnchorLog();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            body.put("status", "UNAVAILABLE");
            body.put("error", "anchor log request interrupted");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        } catch (Exception e) {
            body.put("status", "UNAVAILABLE");
            body.put("error", "anchor log unavailable: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
        var chain = chainService.getChain();
        Map<Long, String> hashes = chain.stream().collect(Collectors.toMap(
                entry -> entry.getSequenceNumber(), entry -> entry.getCurrentHash()));
        String tip = chain.isEmpty() ? "" : chain.get(chain.size() - 1).getCurrentHash();
        var result = AnchorLogVerifier.verify(checkpointService, chain.size(), tip, hashes, log);
        body.put("status", result.valid() ? "VERIFIED" : "MISMATCH");
        body.put("valid", result.valid());
        body.put("anchorCount", result.anchorCount());
        body.put("chainSize", chain.size());
        body.put("error", result.error());
        return ResponseEntity.status(result.valid() ? HttpStatus.OK : HttpStatus.CONFLICT).body(body);
    }

    private String fetchAnchorLog() throws Exception {
        long now = System.currentTimeMillis();
        String cached = cachedAnchorLog;
        if (cached != null && now - cachedAnchorLogAtMillis < ANCHOR_CACHE_MILLIS) {
            return cached;
        }
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(anchorLogUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "text/plain")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        cachedAnchorLog = response.body();
        cachedAnchorLogAtMillis = now;
        return cachedAnchorLog;
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

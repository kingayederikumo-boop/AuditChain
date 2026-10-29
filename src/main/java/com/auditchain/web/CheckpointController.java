package com.auditchain.web;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.service.ChainService;
import com.auditchain.model.AuditEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    private final CheckpointService checkpointService;
    private final ChainService chainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckpointController(CheckpointService checkpointService, ChainService chainService) {
        this.checkpointService = checkpointService;
        this.chainService = chainService;
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

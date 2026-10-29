package com.auditchain.web;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.service.ChainService;
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

    public CheckpointController(CheckpointService checkpointService, ChainService chainService) {
        this.checkpointService = checkpointService;
        this.chainService = chainService;
    }

    @PostMapping
    public Map<String, Object> create() {
        var cp = checkpointService.createAndPublish(chainService.size(), chainService.getLatestHash());
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

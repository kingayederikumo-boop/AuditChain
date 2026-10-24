package com.auditchain.web;

import com.auditchain.model.AuditEntry;
import com.auditchain.service.ChainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChainController {

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    @GetMapping("/chain")
    public Map<String, Object> getChain() {
        List<AuditEntry> chain = chainService.getChain();
        Optional<Long> broken = chainService.verify();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", chain.size());
        body.put("valid", broken.isEmpty());
        body.put("breakSequence", broken.orElse(null));
        body.put("latestHash", chainService.getLatestHash());
        body.put("pendingSync", chainService.pendingSyncCount());
        body.put("entries", chain.stream().map(this::toView).collect(Collectors.toList()));
        return body;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam(defaultValue = "5") int puCount) {
        chainService.generateSynthetic(puCount);
        return getChain();
    }

    @PostMapping("/tamper")
    public ResponseEntity<Map<String, Object>> tamper(@RequestParam(defaultValue = "2") int sequence) {
        boolean ok = chainService.tamper(sequence);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "sequence out of range",
                    "size", chainService.size()
            ));
        }
        return ResponseEntity.ok(getChain());
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        chainService.reset();
        return getChain();
    }

    private Map<String, Object> toView(AuditEntry e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sequence", e.getSequenceNumber());
        m.put("role", e.getActorRole() != null ? e.getActorRole().name() : null);
        m.put("actorId", e.getActorId());
        m.put("location", e.getLocationCode());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        m.put("previousHash", shorten(e.getPreviousHash()));
        m.put("currentHash", shorten(e.getCurrentHash()));
        m.put("payload", e.getPayload());
        m.put("timestamp", e.getTimestamp() != null ? e.getTimestamp().toString() : null);
        return m;
    }

    private String shorten(String hash) {
        if (hash == null) return null;
        return hash.length() > 16 ? hash.substring(0, 16) + "..." : hash;
    }
}

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

    static final int DEFAULT_LIMIT = 200;
    static final int MAX_LIMIT = 1000;

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    /**
     * Returns the chain verdict plus a window of entries. Default window is the newest {@value #DEFAULT_LIMIT}
     * entries (ascending order); use offset/limit to page. The validity verdict always covers the whole chain.
     */
    @GetMapping("/chain")
    public Map<String, Object> getChain(@RequestParam(required = false) Integer limit,
                                        @RequestParam(required = false) Integer offset) {
        return chainView(limit, offset);
    }

    public Map<String, Object> getChain() {
        return chainView(null, null);
    }

    private Map<String, Object> chainView(Integer limitParam, Integer offsetParam) {
        List<AuditEntry> chain = chainService.getChain();
        Optional<Long> broken = chainService.verify();

        int size = chain.size();
        int limit = limitParam == null ? DEFAULT_LIMIT : Math.max(1, Math.min(MAX_LIMIT, limitParam));
        int offset = offsetParam == null ? Math.max(0, size - limit) : Math.max(0, Math.min(size, offsetParam));
        List<AuditEntry> window = chain.subList(offset, Math.min(size, offset + limit));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", size);
        body.put("valid", broken.isEmpty());
        body.put("breakSequence", broken.orElse(null));
        body.put("latestHash", chainService.getLatestHash());
        body.put("pendingSync", chainService.pendingSyncCount());
        body.put("offset", offset);
        body.put("limit", limit);
        body.put("entriesReturned", window.size());
        body.put("entries", window.stream().map(this::toView).collect(Collectors.toList()));
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
        m.put("id", e.getId() != null ? e.getId().toString() : null);
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

package com.auditchain.service;

import com.auditchain.generator.SyntheticDataGenerator;
import com.auditchain.model.AuditEntry;
import com.auditchain.persistence.AuditStore;
import com.auditchain.persistence.PersistentHashChainEngine;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChainService {

    private final PersistentHashChainEngine engine;
    private final boolean demoMode;

    public ChainService(AuditStore auditStore,
                        @Value("${auditchain.demo.enabled:false}") boolean demoMode) {
        this.demoMode = demoMode;
        this.engine = new PersistentHashChainEngine(auditStore);
        if (engine.size() == 0 && demoMode) {
            runGeneratorInto(engine, 3);
        }
    }

    public synchronized List<AuditEntry> getChain() {
        return engine.getChain();
    }

    public synchronized Optional<Long> verify() {
        return engine.verifyChain();
    }

    public synchronized String getLatestHash() {
        return engine.getLatestHash();
    }

    public synchronized int size() {
        return engine.size();
    }

    public synchronized int pendingSyncCount() {
        return engine.getPendingSync().size();
    }

    public synchronized void generateSynthetic(int puCount) {
        requireDemoMode();
        runGeneratorInto(engine, Math.max(1, Math.min(puCount, 20)));
    }

    public synchronized boolean tamper(int sequence) {
        requireDemoMode();
        List<AuditEntry> chain = engine.getChain();
        if (sequence < 1 || sequence > chain.size()) return false;
        chain.get(sequence - 1).setPayload("{\"tampered\":true,\"votes_cast\":99999,\"note\":\"simulated attack\"}");
        return true;
    }

    public synchronized void reset() {
        requireDemoMode();
        generateSynthetic(3);
    }

    /** Append an evidence-linked entry. Used by EvidenceService. Synchronized. */
    public synchronized AuditEntry appendEvidence(String payloadJson, String actorId, String locationCode) {
        return engine.append(payloadJson, AuditEntry.ActorRole.PU_OFFICER, actorId, locationCode);
    }

    private void requireDemoMode() {
        if (!demoMode) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Demo-only operation disabled");
    }

    @PreDestroy
    public void shutdown() {
        if (engine != null) engine.close();
    }

    private void runGeneratorInto(PersistentHashChainEngine eng, int puCount) {
        SyntheticDataGenerator gen = new SyntheticDataGenerator();
        var puCodes = gen.generatePuCodes(puCount);
        var puResults = new java.util.ArrayList<SyntheticDataGenerator.PuResult>();
        for (String pu : puCodes) {
            var r = gen.randomPuResult(pu);
            puResults.add(r);
            eng.append(r.toJson("PU"), AuditEntry.ActorRole.PU_OFFICER, "officer-" + pu.toLowerCase(), pu);
        }
        var lga = gen.aggregateLga("LGA-01", puResults);
        eng.append(lga.toJson(), AuditEntry.ActorRole.LGA_OFFICER, "officer-lga-01", "LGA-01");
        var state = gen.aggregateState("STATE-ENUGU", List.of(lga));
        eng.append(state.toJson(), AuditEntry.ActorRole.STATE_OFFICER, "officer-state-enugu", "STATE-ENUGU");
    }
}

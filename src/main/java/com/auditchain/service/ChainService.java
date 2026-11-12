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
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChainService {

    private static final long PENDING_TTL_MS = 5_000;

    private final PersistentHashChainEngine engine;
    private final boolean demoMode;

    /** Incremented on every change to the chain so callers can cache work that depends on it. */
    private final AtomicLong version = new AtomicLong();
    private volatile int cachedPending;
    private volatile long cachedPendingAtMs;

    public ChainService(AuditStore auditStore,
                        @Value("${auditchain.demo.enabled:false}") boolean demoMode) {
        this.demoMode = demoMode;
        this.engine = new PersistentHashChainEngine(auditStore);
        if (engine.size() == 0 && demoMode) {
            runGeneratorInto(engine, 3);
        }
    }

    public long version() {
        return version.get();
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

    /**
     * Number of entries waiting for sync. This reads the store, so the value is cached for a few seconds
     * and read outside this service's lock: one slow database round trip must not queue every request.
     */
    public int pendingSyncCount() {
        long now = System.currentTimeMillis();
        if (now - cachedPendingAtMs > PENDING_TTL_MS) {
            int fresh = engine.getPendingSync().size();
            cachedPending = fresh;
            cachedPendingAtMs = now;
        }
        return cachedPending;
    }

    /** Refresh after a transaction that committed an audit entry outside the in-memory engine. */
    public synchronized void reloadFromStore() {
        engine.reload();
        version.incrementAndGet();
    }

    public synchronized void generateSynthetic(int puCount) {
        requireDemoMode();
        runGeneratorInto(engine, Math.max(1, Math.min(puCount, 20)));
        version.incrementAndGet();
    }

    public synchronized boolean tamper(int sequence) {
        requireDemoMode();
        List<AuditEntry> chain = engine.getChain();
        if (sequence < 1 || sequence > chain.size()) return false;
        chain.get(sequence - 1).setPayload("{\"tampered\":true,\"votes_cast\":99999,\"note\":\"simulated attack\"}");
        version.incrementAndGet();
        return true;
    }

    public synchronized void reset() {
        requireDemoMode();
        generateSynthetic(3);
    }

    /** Append an evidence-linked entry. Used by EvidenceService. Synchronized. */
    public synchronized AuditEntry appendEvidence(String payloadJson, String actorId, String locationCode) {
        AuditEntry entry = engine.append(payloadJson, AuditEntry.ActorRole.PU_OFFICER, actorId, locationCode);
        version.incrementAndGet();
        return entry;
    }

    /** Append system-generated integrity events such as AI checks and review decisions. */
    public synchronized AuditEntry appendSystemEvent(String payloadJson, String actorId, String locationCode) {
        AuditEntry entry = engine.append(payloadJson, AuditEntry.ActorRole.AUDITOR, actorId, locationCode);
        version.incrementAndGet();
        return entry;
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

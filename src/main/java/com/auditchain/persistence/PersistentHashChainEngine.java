package com.auditchain.persistence;

import com.auditchain.core.HashChainEngine;
import com.auditchain.model.AuditEntry;

import java.util.List;
import java.util.Optional;

/**
 * Wraps the pure HashChainEngine with durable storage and offline queue semantics.
 *
 * - New entries are written to the store with status PENDING_SYNC.
 * - On construction the chain is loaded from the store so restarts continue correctly.
 * - Verification still uses the in-memory engine for speed; the store is the source of truth.
 */
public class PersistentHashChainEngine {

    private final HashChainEngine engine;
    private final AuditStore store;

    public PersistentHashChainEngine(AuditStore store) {
        this.store = store;
        this.engine = new HashChainEngine();
        List<AuditEntry> existing = store.loadAll();
        for (AuditEntry e : existing) {
            engine.restoreEntry(e);
        }
    }

    public AuditEntry append(String payloadJson,
                             AuditEntry.ActorRole role,
                             String actorId,
                             String locationCode) {

        // Build the candidate on a temporary engine so a failed durable write
        // cannot leave the live in-memory chain ahead of persisted storage.
        HashChainEngine candidate = new HashChainEngine();
        for (AuditEntry existing : engine.getChain()) {
            candidate.restoreEntry(existing);
        }
        AuditEntry entry = candidate.append(payloadJson, role, actorId, locationCode);
        store.append(entry);
        engine.restoreEntry(entry);
        return entry;
    }

    public Optional<Long> verifyChain() {
        return engine.verifyChain();
    }

    public List<AuditEntry> getChain() {
        return engine.getChain();
    }

    public int size() {
        return engine.size();
    }

    public String getLatestHash() {
        return engine.getLatestHash();
    }

    public List<AuditEntry> getPendingSync() {
        return store.findPendingSync();
    }

    public void markSynced(List<AuditEntry> entries) {
        for (AuditEntry e : entries) {
            store.markSynced(e.getId());
            e.setStatus(AuditEntry.EntryStatus.SYNCED);
        }
    }

    public void close() {
        store.close();
    }
}

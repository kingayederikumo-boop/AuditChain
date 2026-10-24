package com.auditchain.persistence;

import com.auditchain.model.AuditEntry;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for persisting the hash chain.
 * Implementations: in-memory (testing), SQLite (offline), later PostgreSQL.
 */
public interface AuditStore {

    /**
     * Append a fully computed entry. Must be called in sequence order.
     */
    void append(AuditEntry entry);

    /**
     * Load the entire chain in sequence order.
     */
    List<AuditEntry> loadAll();

    /**
     * Return the latest entry (tip of the chain), if any.
     */
    Optional<AuditEntry> getLatest();

    /**
     * Number of persisted entries.
     */
    int size();

    /**
     * Entries still waiting to be synchronised to a higher node / central store.
     */
    List<AuditEntry> findPendingSync();

    /**
     * Mark an entry as successfully synchronised.
     */
    void markSynced(java.util.UUID entryId);

    /**
     * Close resources.
     */
    void close();
}

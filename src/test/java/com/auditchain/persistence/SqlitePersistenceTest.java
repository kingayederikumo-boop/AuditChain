package com.auditchain.persistence;

import com.auditchain.model.AuditEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class SqlitePersistenceTest {
    @TempDir Path tempDir;

    @Test void entriesSurviveReopeningDatabase() {
        Path db = tempDir.resolve("chain.db");
        PersistentHashChainEngine first = new PersistentHashChainEngine(new SqliteAuditStore(db));
        first.append("synthetic test payload", AuditEntry.ActorRole.PU_OFFICER, "observer-test", "PU-TEST");
        String hash = first.getLatestHash();
        first.close();

        PersistentHashChainEngine second = new PersistentHashChainEngine(new SqliteAuditStore(db));
        assertEquals(1, second.size());
        assertEquals(hash, second.getLatestHash());
        assertTrue(second.verifyChain().isEmpty());
        second.close();
    }

    @Test void emptyStoreIsEmpty() {
        SqliteAuditStore store = SqliteAuditStore.inMemory();
        assertEquals(0, store.size());
        store.close();
    }
}

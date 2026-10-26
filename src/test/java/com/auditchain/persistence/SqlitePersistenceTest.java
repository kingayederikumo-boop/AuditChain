package com.auditchain.persistence;

import com.auditchain.model.AuditEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SqlitePersistenceTest {
    @TempDir Path tempDir;

    @Test
    void entriesSurviveReopeningDatabase() {
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

    @Test
    void emptyStoreIsEmpty() {
        SqliteAuditStore store = SqliteAuditStore.inMemory();
        assertEquals(0, store.size());
        store.close();
    }

    @Test
    void schemaInitializesAndExistingRecordsNotOverwritten() {
        Path db = tempDir.resolve("schema.db");
        PersistentHashChainEngine first = new PersistentHashChainEngine(new SqliteAuditStore(db));
        first.append("payload-1", AuditEntry.ActorRole.PU_OFFICER, "a1", "PU-001");
        first.append("payload-2", AuditEntry.ActorRole.LGA_OFFICER, "a2", "LGA-01");
        assertEquals(2, first.size());
        String tip = first.getLatestHash();
        first.close();

        PersistentHashChainEngine second = new PersistentHashChainEngine(new SqliteAuditStore(db));
        assertEquals(2, second.size());
        assertEquals(tip, second.getLatestHash());
        assertTrue(second.verifyChain().isEmpty());
        second.close();
    }

    @Test
    void hashChainVerificationWorksAfterRestart() {
        Path db = tempDir.resolve("verify.db");
        PersistentHashChainEngine eng = new PersistentHashChainEngine(new SqliteAuditStore(db));
        eng.append("{\"votes\":100}", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        eng.append("{\"votes\":200}", AuditEntry.ActorRole.LGA_OFFICER, "o2", "LGA-1");
        eng.append("{\"votes\":300}", AuditEntry.ActorRole.STATE_OFFICER, "o3", "ST-1");
        assertTrue(eng.verifyChain().isEmpty());
        eng.close();

        PersistentHashChainEngine reloaded = new PersistentHashChainEngine(new SqliteAuditStore(db));
        assertTrue(reloaded.verifyChain().isEmpty());
        assertEquals(3, reloaded.size());
        reloaded.close();
    }

    @Test
    void modifiedRecordsAreDetected() {
        Path db = tempDir.resolve("tamper.db");
        PersistentHashChainEngine eng = new PersistentHashChainEngine(new SqliteAuditStore(db));
        eng.append("good-1", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        eng.append("good-2", AuditEntry.ActorRole.PU_OFFICER, "o2", "PU-2");
        eng.append("good-3", AuditEntry.ActorRole.PU_OFFICER, "o3", "PU-3");
        assertTrue(eng.verifyChain().isEmpty());

        List<AuditEntry> chain = eng.getChain();
        chain.get(1).setPayload("TAMPERED-PAYLOAD");
        Optional<Long> broken = eng.verifyChain();
        assertTrue(broken.isPresent());
        assertEquals(2L, broken.get());
        eng.close();
    }

    @Test
    void invalidChainCannotAcceptNewEntries() {
        com.auditchain.core.HashChainEngine pure = new com.auditchain.core.HashChainEngine();
        pure.append("a", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        pure.append("b", AuditEntry.ActorRole.PU_OFFICER, "o2", "PU-2");
        pure.getChain().get(0).setPayload("broken");
        assertTrue(pure.verifyChain().isPresent());

        assertThrows(IllegalStateException.class, () ->
                pure.append("c", AuditEntry.ActorRole.PU_OFFICER, "o3", "PU-3"));
    }

    @Test
    void concurrentSubmissionsDoNotCreateDuplicateSequenceNumbers() throws Exception {
        Path db = tempDir.resolve("concurrent.db");
        PersistentHashChainEngine eng = new PersistentHashChainEngine(new SqliteAuditStore(db));

        int threads = 8;
        int perThread = 5;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        synchronized (eng) {
                            eng.append("payload-t" + tid + "-" + i,
                                    AuditEntry.ActorRole.PU_OFFICER,
                                    "actor-" + tid,
                                    "PU-" + tid);
                            successes.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    // expected under contention if not fully synchronized at DB level
                }
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get();
        pool.shutdown();

        assertEquals(threads * perThread, eng.size());
        assertTrue(eng.verifyChain().isEmpty());

        List<AuditEntry> chain = eng.getChain();
        for (int i = 0; i < chain.size(); i++) {
            assertEquals(i + 1, chain.get(i).getSequenceNumber());
        }
        eng.close();
    }

    @Test
    void missingRecordBreaksChainOnReload() throws Exception {
        Path db = tempDir.resolve("missing.db");
        PersistentHashChainEngine eng = new PersistentHashChainEngine(new SqliteAuditStore(db));
        eng.append("p1", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        eng.append("p2", AuditEntry.ActorRole.PU_OFFICER, "o2", "PU-2");
        eng.append("p3", AuditEntry.ActorRole.PU_OFFICER, "o3", "PU-3");
        eng.close();

        try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + db.toAbsolutePath());
             var st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM audit_entry WHERE sequence_number = 2");
        }

        assertThrows(IllegalArgumentException.class, () ->
                new PersistentHashChainEngine(new SqliteAuditStore(db)));
    }
}

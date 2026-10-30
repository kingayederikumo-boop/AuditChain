package com.auditchain.persistence;

import com.auditchain.evidence.EvidenceSubmission;
import com.auditchain.evidence.PostgresEvidenceStore;
import com.auditchain.model.AuditEntry;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests against a real PostgreSQL (Testcontainers).
 * Skipped automatically when Docker is unavailable (local sandbox).
 * Runs on GitHub Actions where Docker is present.
 */
@Testcontainers
@EnabledIf("com.auditchain.persistence.PostgresAuditStoreTest#dockerAvailable")
class PostgresAuditStoreTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("auditchain")
            .withUsername("auditchain")
            .withPassword("auditchain");

    private DataSource dataSource;
    private PostgresAuditStore store;
    private PersistentHashChainEngine engine;

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        dataSource = org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .driverClassName("org.postgresql.Driver")
                .build();

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("TRUNCATE TABLE evidence_submission");
            st.execute("TRUNCATE TABLE review_item");
            st.execute("TRUNCATE TABLE audit_entry");
        }

        store = new PostgresAuditStore(dataSource);
        engine = new PersistentHashChainEngine(store);
    }

    @Test
    void appendAndReloadPreservesChain() {
        engine.append("{\"votes\":42}", AuditEntry.ActorRole.PU_OFFICER, "officer-1", "PU-001");
        String tip = engine.getLatestHash();
        assertEquals(1, engine.size());
        assertTrue(engine.verifyChain().isEmpty());

        PersistentHashChainEngine reloaded = new PersistentHashChainEngine(store);
        assertEquals(1, reloaded.size());
        assertEquals(tip, reloaded.getLatestHash());
        assertTrue(reloaded.verifyChain().isEmpty());
    }

    @Test
    void concurrentAppendsProduceUniqueSequences() throws Exception {
        int n = 20;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < n; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    synchronized (engine) {
                        engine.append("payload-" + idx, AuditEntry.ActorRole.PU_OFFICER, "a" + idx, "PU-" + idx);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        start.countDown();
        for (var f : futures) f.get();
        pool.shutdown();

        assertEquals(n, engine.size());
        assertTrue(engine.verifyChain().isEmpty());
        for (int i = 0; i < n; i++) {
            assertEquals(i + 1, engine.getChain().get(i).getSequenceNumber());
        }
    }

    @Test
    void atomicEvidenceWriteRollsBackChainEntryWhenEvidenceInsertFails() {
        PostgresEvidenceStore evidenceStore = new PostgresEvidenceStore(dataSource);
        EvidenceSubmission first = submission("same-idempotency-key");
        evidenceStore.saveAtomically("{\"type\":\"evidence\"}", "officer-a", "PU-01", first);

        EvidenceSubmission duplicate = submission("same-idempotency-key");
        assertThrows(IllegalStateException.class, () ->
                evidenceStore.saveAtomically("{\"type\":\"evidence\"}", "officer-a", "PU-01", duplicate));

        assertEquals(1, store.size(), "failed evidence insert must roll back its corresponding audit entry");
        assertEquals(1, evidenceStore.findByIdempotency("same-idempotency-key").stream().count());
        assertTrue(new PersistentHashChainEngine(store).verifyChain().isEmpty());
    }

    private EvidenceSubmission submission(String idempotencyKey) {
        EvidenceSubmission sub = new EvidenceSubmission();
        sub.setId(UUID.randomUUID());
        sub.setIdempotencyKey(idempotencyKey);
        sub.setActorId("officer-a");
        sub.setLocationCode("PU-01");
        sub.setEvidenceFingerprint("a".repeat(64));
        sub.setContentType("image/png");
        sub.setSizeBytes(9);
        sub.setPayloadJson("{\"votes\":42}");
        sub.setEvidenceBytes(new byte[]{(byte)137, 80, 78, 71, 13, 10, 26, 10, 0});
        return sub;
    }

    @Test
    void duplicateSequenceIsRejectedByConstraint() {
        engine.append("first", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        AuditEntry forged = new AuditEntry();
        forged.setSequenceNumber(1);
        forged.setPreviousHash(engine.getLatestHash());
        forged.setPayload("collision");
        forged.setActorRole(AuditEntry.ActorRole.PU_OFFICER);
        forged.setActorId("evil");
        forged.setLocationCode("X");
        forged.setCurrentHash("deadbeef");
        forged.setStatus(AuditEntry.EntryStatus.PENDING_SYNC);

        assertThrows(IllegalStateException.class, () -> store.append(forged));
    }

    @Test
    void getLatestAndPendingSync() {
        engine.append("p1", AuditEntry.ActorRole.PU_OFFICER, "o1", "PU-1");
        Optional<AuditEntry> latest = store.getLatest();
        assertTrue(latest.isPresent());
        assertEquals(1, latest.get().getSequenceNumber());
        assertEquals(1, store.findPendingSync().size());
    }
}

package com.auditchain.persistence;

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
import java.util.Optional;

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
    void setUp() {
        DataSource ds = org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .driverClassName("org.postgresql.Driver")
                .build();

        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        store = new PostgresAuditStore(ds);
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

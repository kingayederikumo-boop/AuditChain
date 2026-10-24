package com.auditchain.core;

import com.auditchain.model.AuditEntry;
import com.auditchain.persistence.PersistentHashChainEngine;
import com.auditchain.persistence.SqliteAuditStore;

import java.util.List;
import java.util.Optional;

/**
 * Demonstration of both pure in-memory chain and persistent offline-capable chain.
 */
public class HashChainDemo {

    public static void main(String[] args) {
        System.out.println("=== AuditChain - Hash Chain Demo ===\n");

        demoInMemory();
        System.out.println("\n----------------------------------------\n");
        demoPersistent();
    }

    private static void demoInMemory() {
        System.out.println(">>> IN-MEMORY ENGINE");
        HashChainEngine engine = new HashChainEngine();

        System.out.println("1. Appending legitimate entries...");
        engine.append(
                "{\"polling_unit_code\":\"PU-001\",\"votes_cast\":312,\"party_results\":{\"A\":180,\"B\":132}}",
                AuditEntry.ActorRole.PU_OFFICER,
                "officer-pu-001",
                "PU-001"
        );
        engine.append(
                "{\"polling_unit_code\":\"PU-001\",\"votes_cast\":312,\"party_results\":{\"A\":180,\"B\":132},\"level\":\"LGA\"}",
                AuditEntry.ActorRole.LGA_OFFICER,
                "officer-lga-01",
                "LGA-01"
        );
        engine.append(
                "{\"polling_unit_code\":\"PU-001\",\"votes_cast\":312,\"party_results\":{\"A\":180,\"B\":132},\"level\":\"STATE\"}",
                AuditEntry.ActorRole.STATE_OFFICER,
                "officer-state-01",
                "STATE-01"
        );

        System.out.println("   Chain size: " + engine.size());
        System.out.println("   Latest hash: " + engine.getLatestHash().substring(0, 16) + "...");

        System.out.println("2. Verifying clean chain...");
        Optional<Long> breakPoint = engine.verifyChain();
        System.out.println(breakPoint.isEmpty()
                ? "   [OK] Chain is VALID"
                : "   [FAIL] Broken at sequence: " + breakPoint.get());

        System.out.println("3. Simulating tampering on entry #2...");
        AuditEntry second = engine.getChain().get(1);
        second.setPayload("{\"polling_unit_code\":\"PU-001\",\"votes_cast\":999,\"party_results\":{\"A\":900,\"B\":99}}");

        System.out.println("4. Re-verifying after tampering...");
        breakPoint = engine.verifyChain();
        if (breakPoint.isEmpty()) {
            System.out.println("   [OK] Chain is VALID (unexpected!)");
        } else {
            System.out.println("   [DETECTED] TAMPERING at sequence: " + breakPoint.get());
        }
    }

    private static void demoPersistent() {
        System.out.println(">>> PERSISTENT + OFFLINE QUEUE (SQLite in-memory for demo)");

        SqliteAuditStore store = SqliteAuditStore.inMemory();
        PersistentHashChainEngine engine = new PersistentHashChainEngine(store);

        System.out.println("1. Appending entries (they land in offline queue)...");
        engine.append(
                "{\"polling_unit_code\":\"PU-042\",\"votes_cast\":187,\"party_results\":{\"A\":102,\"B\":85}}",
                AuditEntry.ActorRole.PU_OFFICER,
                "officer-pu-042",
                "PU-042"
        );
        engine.append(
                "{\"polling_unit_code\":\"PU-042\",\"votes_cast\":187,\"party_results\":{\"A\":102,\"B\":85},\"level\":\"LGA\"}",
                AuditEntry.ActorRole.LGA_OFFICER,
                "officer-lga-07",
                "LGA-07"
        );

        System.out.println("   Chain size: " + engine.size());
        System.out.println("   Pending sync: " + engine.getPendingSync().size());

        System.out.println("2. Verifying persistent chain...");
        Optional<Long> breakPoint = engine.verifyChain();
        System.out.println(breakPoint.isEmpty()
                ? "   [OK] Chain is VALID"
                : "   [FAIL] Broken at sequence: " + breakPoint.get());

        System.out.println("3. Simulating successful sync of pending entries...");
        List<AuditEntry> pending = engine.getPendingSync();
        engine.markSynced(pending);
        System.out.println("   Pending after markSynced: " + engine.getPendingSync().size());

        System.out.println("4. Reload from same store (simulates process continuity)...");
        PersistentHashChainEngine reloaded = new PersistentHashChainEngine(store);
        System.out.println("   Reloaded size: " + reloaded.size());
        System.out.println("   Latest hash: " + reloaded.getLatestHash().substring(0, 16) + "...");
        Optional<Long> afterReload = reloaded.verifyChain();
        System.out.println(afterReload.isEmpty()
                ? "   [OK] Chain still VALID after reload"
                : "   [FAIL] Broken after reload at: " + afterReload.get());

        engine.close();
        System.out.println("\n=== Demo complete ===");
    }
}

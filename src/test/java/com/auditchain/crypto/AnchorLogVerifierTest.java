package com.auditchain.crypto;

import com.auditchain.core.HashChainEngine;
import com.auditchain.model.AuditEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AnchorLogVerifierTest {

    private static HashChainEngine chain(String... payloads) {
        HashChainEngine engine = new HashChainEngine();
        for (String payload : payloads) {
            engine.append(payload, AuditEntry.ActorRole.PU_OFFICER, "officer", "PU-1");
        }
        return engine;
    }

    private static Map<Long, String> liveHashes(HashChainEngine engine) {
        return engine.getChain().stream().collect(
                Collectors.toMap(entry -> entry.getSequenceNumber(), entry -> entry.getCurrentHash()));
    }

    private static String record(CheckpointService.Checkpoint cp) {
        return String.format("{\"anchoredAt\":\"%s\",\"checkpoint\":{\"sequence\":%d,\"chainTipHash\":\"%s\","
                        + "\"createdAt\":\"%s\",\"signatureHex\":\"%s\",\"publicKeyHex\":\"%s\",\"valid\":true}}",
                Instant.now(), cp.sequence, cp.chainTipHash, cp.createdAt, cp.signatureHex, cp.publicKeyHex);
    }

    private static AnchorLogVerifier.Result check(CheckpointService service, HashChainEngine live, String log) {
        return AnchorLogVerifier.verify(service, live.size(), live.getLatestHash(), liveHashes(live), log);
    }

    @Test
    void acceptsAnAnchorThatStillMatchesAfterTheChainHasGrown() {
        CheckpointService service = new CheckpointService("");
        HashChainEngine live = chain("a", "b", "c");
        var anchor = service.createAndPublish(3, live.getLatestHash());
        live.append("d", AuditEntry.ActorRole.PU_OFFICER, "officer", "PU-1");
        live.append("e", AuditEntry.ActorRole.PU_OFFICER, "officer", "PU-1");

        var result = check(service, live, record(anchor));

        assertTrue(result.valid(), result.error());
        assertEquals(1, result.anchorCount());
    }

    @Test
    void catchesConsistentlyRehashedHistoryThatInternalVerificationAccepts() {
        CheckpointService service = new CheckpointService("");
        HashChainEngine honest = chain("a", "b", "c", "d", "e");
        var anchor = service.createAndPublish(3, honest.getChain().get(2).getCurrentHash());
        String log = record(anchor);

        HashChainEngine rewritten = chain("a", "TAMPERED", "c", "d", "e");

        assertTrue(rewritten.verifyChain().isEmpty(), "a re-hashed chain passes its own verification");
        var result = check(service, rewritten, log);
        assertFalse(result.valid());
        assertTrue(result.error().contains("sequence 3"), result.error());
    }

    @Test
    void rejectsAnAnchorSignedByAnotherKey() {
        CheckpointService trusted = new CheckpointService("");
        CheckpointService attacker = new CheckpointService("");
        HashChainEngine live = chain("a", "b");
        var forged = attacker.createAndPublish(2, live.getLatestHash());

        var result = check(trusted, live, record(forged));

        assertFalse(result.valid());
        assertTrue(result.error().contains("signature invalid"), result.error());
    }

    @Test
    void rejectsAnAnchorBeyondTheLiveChain() {
        CheckpointService service = new CheckpointService("");
        HashChainEngine live = chain("a");
        var anchor = service.createAndPublish(5, "some-tip");

        var result = check(service, live, record(anchor));

        assertFalse(result.valid());
        assertTrue(result.error().contains("outside live chain"), result.error());
    }

    @Test
    void acceptsAGenesisAnchorOnAnEmptyChain() {
        CheckpointService service = new CheckpointService("");
        HashChainEngine live = chain();
        var anchor = service.createAndPublish(0, "0".repeat(64));

        var result = check(service, live, record(anchor));

        assertTrue(result.valid(), result.error());
    }

    @Test
    void rejectsAnEmptyLogInsteadOfPassingSilently() {
        CheckpointService service = new CheckpointService("");

        var result = check(service, chain("a"), "");

        assertFalse(result.valid());
        assertTrue(result.error().contains("no checkpoints"), result.error());
    }

    @Test
    void reportsTheLineOfAMalformedRecord() {
        CheckpointService service = new CheckpointService("");

        var result = check(service, chain("a"), "this is not json");

        assertFalse(result.valid());
        assertTrue(result.error().contains("line 1"), result.error());
    }

    @Test
    void skipsBlankLinesAndCountsEveryRecord() {
        CheckpointService service = new CheckpointService("");
        HashChainEngine live = chain("a", "b", "c");
        var first = service.createAndPublish(1, live.getChain().get(0).getCurrentHash());
        var second = service.createAndPublish(3, live.getLatestHash());

        var result = check(service, live, record(first) + "\n\n" + record(second) + "\n");

        assertTrue(result.valid(), result.error());
        assertEquals(2, result.anchorCount());
    }
}

package com.auditchain.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointServiceTest {
    @Test
    void signAndVerify() {
        CheckpointService svc = new CheckpointService("");
        var cp = svc.createAndPublish(3, "abc123");
        assertTrue(svc.verify(cp));
        assertEquals(3, cp.sequence);
        assertFalse(cp.signatureHex.isBlank());
        assertEquals(64, svc.publicKeyHex().length());
    }

    @Test
    void detectsTipDivergence() {
        CheckpointService svc = new CheckpointService("");
        var cp = svc.createAndPublish(5, "original-tip");
        var tamper = svc.detectTampering(5, "different-tip", java.util.List.of(cp));
        assertTrue(tamper.isPresent());
    }

    @Test
    void detectsHistoricalRewriteAfterChainHasGrown() {
        CheckpointService svc = new CheckpointService("");
        var cp = svc.createAndPublish(2, "original-sequence-two-hash");
        var liveHashes = java.util.Map.of(
                1L, "hash-one",
                2L, "rewritten-sequence-two-hash",
                3L, "hash-three",
                4L, "hash-four",
                5L, "current-tip"
        );

        var result = svc.detectTampering(5, "current-tip", liveHashes, java.util.List.of(cp));

        assertTrue(result.isPresent());
        assertTrue(result.get().contains("sequence 2"));
    }

    @Test
    void detectsMissingHistoricalHashRatherThanSilentlySkippingCheckpoint() {
        CheckpointService svc = new CheckpointService("");
        var cp = svc.createAndPublish(2, "original-sequence-two-hash");

        var result = svc.detectTampering(5, "current-tip",
                java.util.Map.of(5L, "current-tip"), java.util.List.of(cp));

        assertTrue(result.isPresent());
        assertTrue(result.get().contains("unavailable"));
    }

    @Test
    void rejectsCheckpointSignedByAnUntrustedKey() {
        CheckpointService trusted = new CheckpointService("");
        CheckpointService attacker = new CheckpointService("");
        var forged = attacker.createAndPublish(9, "replacement-tip");

        assertFalse(trusted.verify(forged));
        assertTrue(trusted.detectTampering(9, "replacement-tip", java.util.List.of(forged)).isPresent());
    }

    @Test
    void rejectsModifiedCheckpointPayload() {
        CheckpointService svc = new CheckpointService("");
        var original = svc.createAndPublish(4, "original-tip");
        var modified = new CheckpointService.Checkpoint(
                original.sequence, "modified-tip", original.createdAt,
                original.signatureHex, original.publicKeyHex);

        assertFalse(svc.verify(modified));
    }
}

package com.auditchain.crypto;

import com.auditchain.core.HashChainEngine;
import com.auditchain.model.AuditEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExternalAnchorVerifierTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void detectsHistoryRewriteEvenWhenAttackerRecomputesTheEntryHash() throws Exception {
        HashChainEngine chainEngine = new HashChainEngine();
        AuditEntry original = chainEngine.append(
                "{\"votes\":42}", AuditEntry.ActorRole.PU_OFFICER, "officer:opaque-id", "PU-01");

        CheckpointService signer = new CheckpointService("");
        CheckpointService.Checkpoint checkpoint =
                signer.createAndPublish(original.getSequenceNumber(), original.getCurrentHash());

        // Model a capable attacker who changes an old payload and re-hashes that entry.
        original.setPayload("{\"votes\":99999,\"rewritten\":true}");
        original.setCurrentHash(chainEngine.computeHash(original.toCanonicalString()));
        assertTrue(chainEngine.verifyChain().isEmpty(),
                "the rewritten chain is internally valid after the attacker recomputes its hash");

        String log = mapper.writeValueAsString(Map.of(
                "anchoredAt", checkpoint.createdAt.toString(),
                "checkpoint", Map.of(
                        "sequence", checkpoint.sequence,
                        "chainTipHash", checkpoint.chainTipHash,
                        "createdAt", checkpoint.createdAt.toString(),
                        "signatureHex", checkpoint.signatureHex,
                        "publicKeyHex", checkpoint.publicKeyHex,
                        "valid", true))) + "\n";

        ExternalAnchorVerifier verifier = new ExternalAnchorVerifier("", mapper);
        ExternalAnchorVerifier.Result result = verifier.verifyContent(log, List.of(original), signer);

        assertFalse(result.valid());
        assertEquals(1, result.checkpointCount());
        assertTrue(result.error().contains("diverged"));
    }

    @Test
    void rejectsAnEmptyExternalAnchorLog() {
        ExternalAnchorVerifier verifier = new ExternalAnchorVerifier("", mapper);
        ExternalAnchorVerifier.Result result =
                verifier.verifyContent("", List.of(), new CheckpointService(""));

        assertFalse(result.valid());
        assertEquals(0, result.checkpointCount());
        assertTrue(result.error().contains("empty"));
    }
}

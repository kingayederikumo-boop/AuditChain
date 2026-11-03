package com.auditchain.web;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.crypto.ExternalAnchorVerifier;
import com.auditchain.service.ChainService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckpointControllerTest {

    private static final String ZERO_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    @Test
    void reportsLocalCheckpointStateSeparatelyFromUnavailableExternalAnchors() {
        ChainService chainService = mock(ChainService.class);
        ExternalAnchorVerifier externalVerifier = mock(ExternalAnchorVerifier.class);
        CheckpointService checkpointService = new CheckpointService("");

        when(chainService.getChain()).thenReturn(List.of());
        when(chainService.size()).thenReturn(0);
        when(chainService.getLatestHash()).thenReturn(ZERO_HASH);
        when(externalVerifier.verify(anyList(), eq(checkpointService)))
                .thenReturn(new ExternalAnchorVerifier.Result(false, 0, "external anchor log is empty"));

        CheckpointController controller = new CheckpointController(
                checkpointService, chainService, externalVerifier);

        Map<String, Object> result = controller.verifyPublished();

        assertEquals(true, result.get("internalCheckpointValid"));
        assertNull(result.get("internalCheckpointError"));
        assertEquals(false, result.get("externalAnchorsValid"));
        assertEquals(false, result.get("valid"));
    }

    @Test
    void exposesStoredCheckpointMismatchEvenWhenExternalLogIsUnavailable() {
        ChainService chainService = mock(ChainService.class);
        ExternalAnchorVerifier externalVerifier = mock(ExternalAnchorVerifier.class);
        CheckpointService checkpointService = new CheckpointService("");
        checkpointService.createAndPublish(1, "checkpoint-tip-not-present-in-live-chain");

        when(chainService.getChain()).thenReturn(List.of());
        when(chainService.size()).thenReturn(0);
        when(chainService.getLatestHash()).thenReturn(ZERO_HASH);
        when(externalVerifier.verify(anyList(), eq(checkpointService)))
                .thenReturn(new ExternalAnchorVerifier.Result(false, 0, "external anchor log unavailable"));

        CheckpointController controller = new CheckpointController(
                checkpointService, chainService, externalVerifier);

        Map<String, Object> result = controller.verifyPublished();

        assertEquals(false, result.get("internalCheckpointValid"));
        assertTrue(((String) result.get("internalCheckpointError")).contains("outside live chain"));
        assertEquals(false, result.get("externalAnchorsValid"));
        assertEquals(false, result.get("valid"));
    }
}
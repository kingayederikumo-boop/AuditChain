package com.auditchain.scenario;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.notification.TelegramNotifier;
import com.auditchain.service.ChainService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClimateDisruptionScenarioServiceTest {

    @Test
    void simulatesOutageWithoutClaimingRealDataWasChanged() {
        ChainService chain = mock(ChainService.class);
        when(chain.size()).thenReturn(7);
        when(chain.getLatestHash()).thenReturn("tip-hash");
        TelegramNotifier telegram = mock(TelegramNotifier.class);
        when(telegram.isConfigured()).thenReturn(false);

        ClimateDisruptionScenarioService service = new ClimateDisruptionScenarioService(
                chain, new CheckpointService(""), telegram);

        var result = service.run(120, 12, 240);

        assertEquals("SIMULATED", result.get("status"));
        assertEquals(true, result.get("simulationOnly"));
        assertEquals(240, result.get("reconciledSubmissionsAfterReconnect"));
        assertEquals(0, result.get("simulatedDataLoss"));
        assertEquals(true, ((java.util.Map<?, ?>) result.get("checkpoint")).get("valid"));
        verify(telegram).sendMessage(contains("climate-disruption simulation"));
    }

    @Test
    void rejectsOutOfRangeScenarioInputs() {
        ChainService chain = mock(ChainService.class);
        TelegramNotifier telegram = mock(TelegramNotifier.class);
        ClimateDisruptionScenarioService service = new ClimateDisruptionScenarioService(
                chain, new CheckpointService(""), telegram);

        assertThrows(IllegalArgumentException.class, () -> service.run(0, 12, 240));
        assertThrows(IllegalArgumentException.class, () -> service.run(60, 0, 240));
        assertThrows(IllegalArgumentException.class, () -> service.run(60, 12, -1));
        verifyNoInteractions(telegram);
    }
}

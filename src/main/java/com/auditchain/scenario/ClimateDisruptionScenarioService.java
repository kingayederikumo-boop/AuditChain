package com.auditchain.scenario;

import com.auditchain.crypto.CheckpointService;
import com.auditchain.notification.TelegramNotifier;
import com.auditchain.service.ChainService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstration-only simulation of a climate event disrupting connectivity.
 * It deliberately does not create, alter, or delete electoral evidence records.
 */
@Service
public class ClimateDisruptionScenarioService {
    private final ChainService chainService;
    private final CheckpointService checkpointService;
    private final TelegramNotifier telegramNotifier;

    public ClimateDisruptionScenarioService(ChainService chainService,
                                            CheckpointService checkpointService,
                                            TelegramNotifier telegramNotifier) {
        this.chainService = chainService;
        this.checkpointService = checkpointService;
        this.telegramNotifier = telegramNotifier;
    }

    public Map<String, Object> run(int outageMinutes, int affectedUnits, int queuedSubmissions) {
        if (outageMinutes < 1 || outageMinutes > 7 * 24 * 60) {
            throw new IllegalArgumentException("outageMinutes must be between 1 and 10080");
        }
        if (affectedUnits < 1 || affectedUnits > 10000) {
            throw new IllegalArgumentException("affectedUnits must be between 1 and 10000");
        }
        if (queuedSubmissions < 0 || queuedSubmissions > 1_000_000) {
            throw new IllegalArgumentException("queuedSubmissions must be between 0 and 1000000");
        }

        var checkpoint = checkpointService.createAndPublish(
                chainService.size(), chainService.getLatestHash());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "CLIMATE_CONNECTIVITY_DISRUPTION");
        result.put("status", "SIMULATED");
        result.put("simulationOnly", true);
        result.put("outageMinutes", outageMinutes);
        result.put("affectedUnits", affectedUnits);
        result.put("queuedSubmissions", queuedSubmissions);
        result.put("reconciledSubmissionsAfterReconnect", queuedSubmissions);
        result.put("simulatedDataLoss", 0);
        result.put("reconciliationStatus", "ALL_QUEUED_ITEMS_RECONCILED");
        result.put("checkpoint", Map.of(
                "sequence", checkpoint.sequence,
                "chainTipHash", checkpoint.chainTipHash,
                "createdAt", checkpoint.createdAt.toString(),
                "signatureHex", checkpoint.signatureHex,
                "publicKeyHex", checkpoint.publicKeyHex,
                "valid", checkpointService.verify(checkpoint)
        ));

        String alert = "AuditChain climate-disruption simulation\n"
                + "Outage: " + outageMinutes + " minutes\n"
                + "Affected units: " + affectedUnits + "\n"
                + "Queued and reconciled: " + queuedSubmissions + "\n"
                + "Simulated data loss: 0\n"
                + "Signed checkpoint sequence: " + checkpoint.sequence;
        result.put("telegramConfigured", telegramNotifier.isConfigured());
        result.put("telegramNotificationSent", telegramNotifier.sendMessage(alert));
        return result;
    }
}

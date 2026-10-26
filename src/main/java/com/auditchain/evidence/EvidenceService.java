package com.auditchain.evidence;

import com.auditchain.model.AuditEntry;
import com.auditchain.service.ChainService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accepts synthetic evidence (image bytes or structured payload), fingerprints it,
 * enforces idempotency, appends to the hash chain, and returns a verifiable receipt.
 */
@Service
public class EvidenceService {

    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private final ChainService chainService;
    private final Map<String, EvidenceSubmission> byIdempotency = new ConcurrentHashMap<>();

    public EvidenceService(ChainService chainService) {
        this.chainService = chainService;
    }

    public EvidenceSubmission submit(String idempotencyKey,
                                     String actorId,
                                     String locationCode,
                                     String contentType,
                                     byte[] evidenceBytes,
                                     String structuredPayloadJson) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey required");
        }
        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("actorId required");
        }
        if (evidenceBytes != null && evidenceBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("evidence exceeds " + MAX_BYTES + " bytes");
        }

        EvidenceSubmission existing = byIdempotency.get(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        String fingerprint = sha256Hex(evidenceBytes != null ? evidenceBytes
                : (structuredPayloadJson != null ? structuredPayloadJson.getBytes(StandardCharsets.UTF_8) : new byte[0]));

        String chainPayload = String.format(
                "{\"type\":\"evidence\",\"location\":\"%s\",\"fingerprint\":\"%s\",\"contentType\":\"%s\",\"size\":%d,\"data\":%s}",
                locationCode == null ? "" : locationCode,
                fingerprint,
                contentType == null ? "application/octet-stream" : contentType,
                evidenceBytes == null ? 0 : evidenceBytes.length,
                structuredPayloadJson == null ? "null" : structuredPayloadJson
        );

        AuditEntry entry = chainService.appendEvidence(chainPayload, actorId, locationCode);

        EvidenceSubmission sub = new EvidenceSubmission();
        sub.setIdempotencyKey(idempotencyKey);
        sub.setActorId(actorId);
        sub.setLocationCode(locationCode);
        sub.setEvidenceFingerprint(fingerprint);
        sub.setContentType(contentType);
        sub.setSizeBytes(evidenceBytes == null ? 0 : evidenceBytes.length);
        sub.setPayloadJson(structuredPayloadJson);
        sub.setChainEntryId(entry.getId());
        sub.setReceiptHash(entry.getCurrentHash());

        byIdempotency.put(idempotencyKey, sub);
        return sub;
    }

    public Optional<EvidenceSubmission> findByIdempotency(String key) {
        return Optional.ofNullable(byIdempotency.get(key));
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(data == null ? new byte[0] : data);
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

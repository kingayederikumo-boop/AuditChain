package com.auditchain.evidence;

import com.auditchain.model.AuditEntry;
import com.auditchain.service.ChainService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import javax.sql.DataSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class EvidenceService {

    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private final ChainService chainService;
    private final DurableEvidenceStore store;

    public EvidenceService(ChainService chainService,
                           @Value("${auditchain.evidence.db-path:data/evidence.db}") String evidenceDbPath,
                           @Value("${auditchain.store.type:sqlite}") String storeType,
                           ObjectProvider<DataSource> dataSource) {
        this.chainService = chainService;
        this.store = "postgres".equalsIgnoreCase(storeType)
                ? new PostgresEvidenceStore(dataSource.getObject())
                : new DurableEvidenceStore(Path.of(evidenceDbPath));
    }

    public synchronized EvidenceSubmission submit(String idempotencyKey,
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

        if (evidenceBytes == null || evidenceBytes.length == 0) throw new IllegalArgumentException("Image required");
        if (!java.util.Objects.equals(contentType, detectImageType(evidenceBytes)))
            throw new IllegalArgumentException("Only valid PNG or JPEG image bytes are accepted");
        if (structuredPayloadJson == null || structuredPayloadJson.isBlank())
            throw new IllegalArgumentException("Structured JSON required");
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(structuredPayloadJson);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid structured JSON", ex);
        }
        String fingerprint = sha256Hex(evidenceBytes);
        Optional<EvidenceSubmission> existing = store.findByIdempotency(idempotencyKey);
        if (existing.isPresent()) {
            EvidenceSubmission prior = existing.get();
            if (!prior.getActorId().equals(actorId) ||
                !java.util.Objects.equals(prior.getLocationCode(), locationCode) ||
                !prior.getEvidenceFingerprint().equals(fingerprint) ||
                !java.util.Objects.equals(prior.getPayloadJson(), structuredPayloadJson))
                throw new EvidenceConflictException("Idempotency key already used for different evidence");
            return prior;
        }

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

        store.save(sub);
        return sub;
    }

    static String detectImageType(byte[] bytes) {
        if (bytes == null) return null;
        if (bytes.length >= 8 && Arrays.equals(Arrays.copyOf(bytes, 8),
                new byte[]{(byte)137, 80, 78, 71, 13, 10, 26, 10})) return "image/png";
        if (bytes.length >= 4 && (bytes[0] & 255) == 255 &&
                (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255 &&
                (bytes[bytes.length - 2] & 255) == 255 &&
                (bytes[bytes.length - 1] & 255) == 217) return "image/jpeg";
        return null;
    }

    public Optional<EvidenceSubmission> findByIdempotency(String key) {
        return store.findByIdempotency(key);
    }

    public DurableEvidenceStore store() {
        return store;
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

    @PreDestroy
    public void close() {
        store.close();
    }
}

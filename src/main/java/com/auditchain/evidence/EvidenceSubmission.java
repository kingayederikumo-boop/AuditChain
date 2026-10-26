package com.auditchain.evidence;

import java.time.Instant;
import java.util.UUID;

public class EvidenceSubmission {
    private UUID id;
    private String idempotencyKey;
    private String actorId;
    private String locationCode;
    private String evidenceFingerprint;
    private String contentType;
    private long sizeBytes;
    private String payloadJson;
    private Instant submittedAt;
    private UUID chainEntryId;
    private String receiptHash;

    public EvidenceSubmission() {
        this.id = UUID.randomUUID();
        this.submittedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getEvidenceFingerprint() { return evidenceFingerprint; }
    public void setEvidenceFingerprint(String evidenceFingerprint) { this.evidenceFingerprint = evidenceFingerprint; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public UUID getChainEntryId() { return chainEntryId; }
    public void setChainEntryId(UUID chainEntryId) { this.chainEntryId = chainEntryId; }
    public String getReceiptHash() { return receiptHash; }
    public void setReceiptHash(String receiptHash) { this.receiptHash = receiptHash; }
}

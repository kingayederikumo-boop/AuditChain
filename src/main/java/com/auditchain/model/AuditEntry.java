package com.auditchain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Core data model for a single entry in the AuditChain.
 * Each entry is cryptographically linked to the previous one via previousHash.
 */
public class AuditEntry {

    private UUID id;
    private long sequenceNumber;
    private String previousHash;
    private String payload;
    private ActorRole actorRole;
    private String actorId;
    private Instant timestamp;
    private String currentHash;
    private String signature;
    private String locationCode;
    private EntryStatus status;

    public enum ActorRole {
        PU_OFFICER,
        LGA_OFFICER,
        STATE_OFFICER,
        AUDITOR
    }

    public enum EntryStatus {
        PENDING_SYNC,
        SYNCED,
        VERIFIED
    }

    public AuditEntry() {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.status = EntryStatus.PENDING_SYNC;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public ActorRole getActorRole() { return actorRole; }
    public void setActorRole(ActorRole actorRole) { this.actorRole = actorRole; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public EntryStatus getStatus() { return status; }
    public void setStatus(EntryStatus status) { this.status = status; }

    /**
     * Canonical string used for hashing. Order and format must remain stable.
     */
    public String toCanonicalString() {
        return sequenceNumber + "|"
                + (previousHash != null ? previousHash : "GENESIS") + "|"
                + (payload != null ? payload : "") + "|"
                + (actorRole != null ? actorRole.name() : "") + "|"
                + (actorId != null ? actorId : "") + "|"
                + (timestamp != null ? timestamp.toString() : "") + "|"
                + (locationCode != null ? locationCode : "");
    }
}

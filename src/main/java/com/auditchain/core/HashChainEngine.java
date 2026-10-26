package com.auditchain.core;

import com.auditchain.model.AuditEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core engine responsible for creating and verifying hash-chained audit entries.
 * This is the heart of AuditChain's tamper-evidence guarantee.
 */
public class HashChainEngine {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final List<AuditEntry> chain = new ArrayList<>();

    /**
     * Creates a new entry linked to the current tip of the chain.
     */
    public AuditEntry append(String payloadJson,
                             AuditEntry.ActorRole role,
                             String actorId,
                             String locationCode) {

        if (verifyChain().isPresent()) throw new IllegalStateException("Cannot append to a broken audit chain");
        AuditEntry entry = new AuditEntry();
        entry.setSequenceNumber(chain.size() + 1);
        entry.setPreviousHash(getLatestHash());
        entry.setPayload(payloadJson);
        entry.setActorRole(role);
        entry.setActorId(actorId);
        entry.setLocationCode(locationCode);

        String hash = computeHash(entry.toCanonicalString());
        entry.setCurrentHash(hash);

        chain.add(entry);
        return entry;
    }

    /**
     * Returns the hash of the latest entry, or the genesis hash if the chain is empty.
     */
    public String getLatestHash() {
        if (chain.isEmpty()) {
            return GENESIS_HASH;
        }
        return chain.get(chain.size() - 1).getCurrentHash();
    }

    /**
     * Verifies the entire chain.
     * Returns Optional.empty() if valid, or the sequence number of the first broken link.
     */
    public Optional<Long> verifyChain() {
        String expectedPrevious = GENESIS_HASH;

        long expectedSequence = 1;
        for (AuditEntry entry : chain) {
            if (entry.getSequenceNumber() != expectedSequence++) {
                return Optional.of(expectedSequence - 1);
            }
            if (!expectedPrevious.equals(entry.getPreviousHash())) {
                return Optional.of(entry.getSequenceNumber());
            }

            String recomputed = computeHash(entry.toCanonicalString());
            if (!recomputed.equals(entry.getCurrentHash())) {
                return Optional.of(entry.getSequenceNumber());
            }

            expectedPrevious = entry.getCurrentHash();
        }
        return Optional.empty();
    }

    public List<AuditEntry> getChain() {
        return new ArrayList<>(chain);
    }

    public int size() {
        return chain.size();
    }

    /**
     * Restores a previously persisted entry into the in-memory chain.
     * Used on startup. Does not recompute the hash — trusts the stored values.
     */
    public void restoreEntry(AuditEntry entry) {
        if (entry.getSequenceNumber() != chain.size() + 1) {
            throw new IllegalArgumentException("Restored chain sequence is not contiguous");
        }
        chain.add(entry);
    }

    public String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

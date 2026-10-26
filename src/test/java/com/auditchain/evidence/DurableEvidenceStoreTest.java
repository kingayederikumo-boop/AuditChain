package com.auditchain.evidence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DurableEvidenceStoreTest {
    @TempDir Path temp;

    @Test
    void idempotencySurvivesReopen() {
        Path db = temp.resolve("evidence.db");
        DurableEvidenceStore first = new DurableEvidenceStore(db);
        EvidenceSubmission sub = new EvidenceSubmission();
        sub.setIdempotencyKey("key-1");
        sub.setActorId("officer-1");
        sub.setEvidenceFingerprint("abc");
        sub.setReceiptHash("hash1");
        sub.setChainEntryId(UUID.randomUUID());
        first.save(sub);
        first.close();

        DurableEvidenceStore second = new DurableEvidenceStore(db);
        var found = second.findByIdempotency("key-1");
        assertTrue(found.isPresent());
        assertEquals("abc", found.get().getEvidenceFingerprint());
        assertEquals("officer-1", found.get().getActorId());
        second.close();
    }
}

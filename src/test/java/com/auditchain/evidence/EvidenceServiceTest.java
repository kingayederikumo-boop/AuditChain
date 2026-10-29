package com.auditchain.evidence;

import com.auditchain.persistence.SqliteAuditStore;
import com.auditchain.service.ChainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceServiceTest {
    @TempDir Path temp;

    @Test
    void validatesImageSignatures() {
        byte[] png = new byte[]{(byte)137,80,78,71,13,10,26,10,0};
        byte[] jpeg = new byte[]{(byte)255,(byte)216,(byte)255,0,(byte)255,(byte)217};
        assertEquals("image/png", EvidenceService.detectImageType(png));
        assertEquals("image/jpeg", EvidenceService.detectImageType(jpeg));
        assertNull(EvidenceService.detectImageType("not an image".getBytes()));
        assertNull(EvidenceService.detectImageType(new byte[0]));
    }

    @Test
    void fingerprintIsStableAndContentSensitive() {
        String fp1 = EvidenceService.sha256Hex("hello".getBytes());
        String fp2 = EvidenceService.sha256Hex("hello".getBytes());
        String fp3 = EvidenceService.sha256Hex("world".getBytes());
        assertEquals(fp1, fp2);
        assertNotEquals(fp1, fp3);
        assertEquals(64, fp1.length());
    }

    @Test
    void submissionIsIdempotentAndConflictingReuseIsRejected() {
        ChainService chain = new ChainService(SqliteAuditStore.inMemory(), false);
        EvidenceService service = new EvidenceService(
                chain, temp.resolve("evidence.db").toString(), "sqlite", null);
        byte[] png = new byte[]{(byte)137,80,78,71,13,10,26,10,0};
        String payload = "{\"votes\":42}";

        try {
            EvidenceSubmission first = service.submit(
                    "key-1", "officer-1", "PU-01", "image/png", png, payload);
            EvidenceSubmission retry = service.submit(
                    "key-1", "officer-1", "PU-01", "image/png", png, payload);

            assertEquals(first.getId(), retry.getId());
            assertEquals(first.getReceiptHash(), retry.getReceiptHash());
            assertEquals(1, chain.size());
            assertTrue(Arrays.equals(png,
                    service.findByIdempotency("key-1").orElseThrow().getEvidenceBytes()));

            assertThrows(EvidenceConflictException.class, () -> service.submit(
                    "key-1", "officer-1", "PU-01", "image/png", png, "{\"votes\":43}"));
            assertEquals(1, chain.size());
        } finally {
            service.close();
            chain.shutdown();
        }
    }
}

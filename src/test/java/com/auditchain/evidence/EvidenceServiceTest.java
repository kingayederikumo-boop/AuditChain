package com.auditchain.evidence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EvidenceServiceTest {
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
    void fingerprintAndIdempotency() {
        String fp1 = EvidenceService.sha256Hex("hello".getBytes());
        String fp2 = EvidenceService.sha256Hex("hello".getBytes());
        String fp3 = EvidenceService.sha256Hex("world".getBytes());
        assertEquals(fp1, fp2);
        assertNotEquals(fp1, fp3);
        assertEquals(64, fp1.length());
    }
}

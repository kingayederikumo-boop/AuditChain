package com.auditchain.evidence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EvidenceServiceTest {
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

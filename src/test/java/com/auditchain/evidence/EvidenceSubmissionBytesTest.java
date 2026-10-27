package com.auditchain.evidence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceSubmissionBytesTest {

    @Test
    void storesAndReturnsDefensiveCopiesOfEvidenceBytes() {
        byte[] source = new byte[] {1, 2, 3};
        EvidenceSubmission submission = new EvidenceSubmission();
        submission.setEvidenceBytes(source);

        source[0] = 9;
        assertArrayEquals(new byte[] {1, 2, 3}, submission.getEvidenceBytes());

        byte[] returned = submission.getEvidenceBytes();
        returned[1] = 9;
        assertArrayEquals(new byte[] {1, 2, 3}, submission.getEvidenceBytes());
    }

    @Test
    void recognizesPngSignature() {
        assertEquals("image/png", EvidenceService.detectImageType(new byte[] {
            (byte) 137, 80, 78, 71, 13, 10, 26, 10
        }));
    }

    @Test
    void rejectsUnknownImageSignature() {
        assertNull(EvidenceService.detectImageType(new byte[] {1, 2, 3, 4}));
    }
}

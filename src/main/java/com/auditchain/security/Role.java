package com.auditchain.security;

public enum Role {
    OFFICER,   // submit evidence
    REVIEWER,  // decide review queue
    AUDITOR,   // checkpoints + full read
    ADMIN      // all
}

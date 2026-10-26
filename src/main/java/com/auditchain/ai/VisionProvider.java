package com.auditchain.ai;

/**
 * Provider-neutral vision interface. Implementations must never declare electoral fraud.
 * They may only extract numbers and flag arithmetic inconsistencies for human review.
 */
public interface VisionProvider {
    VisionResult analyse(byte[] imageBytes, String declaredPayload);
}

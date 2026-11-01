package com.auditchain.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies a published, append-only anchor log (JSON Lines) against the live chain.
 *
 * Each line has the form
 * {"anchoredAt":"...","checkpoint":{sequence,chainTipHash,createdAt,signatureHex,publicKeyHex}}.
 * Signatures are checked against this service's own trusted key, never against the
 * key embedded in a record, and each anchored hash is compared with the live chain
 * hash at the anchored sequence.
 */
public final class AnchorLogVerifier {

    public record Result(boolean valid, int anchorCount, String error) { }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AnchorLogVerifier() { }

    /** Parses the log. Blank lines are skipped; a malformed record throws with its line number. */
    public static List<CheckpointService.Checkpoint> parse(String jsonl) {
        List<CheckpointService.Checkpoint> out = new ArrayList<>();
        if (jsonl == null) return out;
        String[] lines = jsonl.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            try {
                JsonNode cp = MAPPER.readTree(line).path("checkpoint");
                if (!cp.hasNonNull("sequence") || !cp.hasNonNull("chainTipHash")
                        || !cp.hasNonNull("createdAt") || !cp.hasNonNull("signatureHex")
                        || !cp.hasNonNull("publicKeyHex")) {
                    throw new IllegalArgumentException("missing checkpoint field");
                }
                out.add(new CheckpointService.Checkpoint(
                        cp.get("sequence").asLong(),
                        cp.get("chainTipHash").asText(),
                        Instant.parse(cp.get("createdAt").asText()),
                        cp.get("signatureHex").asText(),
                        cp.get("publicKeyHex").asText()));
            } catch (Exception e) {
                throw new IllegalArgumentException("malformed anchor record on line " + (i + 1));
            }
        }
        return out;
    }

    public static Result verify(CheckpointService service, long currentSequence, String currentTipHash,
                                Map<Long, String> liveHashes, String jsonl) {
        List<CheckpointService.Checkpoint> anchors;
        try {
            anchors = parse(jsonl);
        } catch (IllegalArgumentException e) {
            return new Result(false, 0, e.getMessage());
        }
        if (anchors.isEmpty()) {
            return new Result(false, 0, "anchor log contains no checkpoints");
        }
        Optional<String> issue = service.detectTampering(currentSequence, currentTipHash, liveHashes, anchors);
        return new Result(issue.isEmpty(), anchors.size(), issue.orElse(null));
    }
}

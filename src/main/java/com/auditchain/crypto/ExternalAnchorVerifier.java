package com.auditchain.crypto;

import com.auditchain.model.AuditEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies independently published checkpoint records against the live chain.
 * A missing or unreadable anchor log is a verification failure, never a pass.
 */
@Service
public class ExternalAnchorVerifier {
    private final String logUrl;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public ExternalAnchorVerifier(
            @Value("${auditchain.anchors.log-url:}") String logUrl,
            ObjectMapper mapper) {
        this.logUrl = logUrl == null ? "" : logUrl.trim();
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Result verify(List<AuditEntry> liveChain, CheckpointService checkpointService) {
        if (logUrl.isBlank()) return Result.failure(0, "external anchor log URL is not configured");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(logUrl))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "text/plain, application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return Result.failure(0, "external anchor log returned HTTP " + response.statusCode());
            }
            return verifyContent(response.body(), liveChain, checkpointService);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Result.failure(0, "external anchor log request interrupted");
        } catch (Exception ex) {
            return Result.failure(0, "external anchor log unavailable: " + ex.getClass().getSimpleName());
        }
    }

    public Result verifyContent(String jsonLines, List<AuditEntry> liveChain,
                                CheckpointService checkpointService) {
        if (jsonLines == null || jsonLines.isBlank()) {
            return Result.failure(0, "external anchor log is empty");
        }

        List<CheckpointService.Checkpoint> checkpoints = new ArrayList<>();
        try {
            for (String line : jsonLines.split("\\R")) {
                if (line.isBlank()) continue;
                JsonNode root = mapper.readTree(line);
                JsonNode node = root.has("checkpoint") ? root.path("checkpoint") : root;
                if (node.path("sequence").isMissingNode()
                        || node.path("chainTipHash").isMissingNode()
                        || node.path("createdAt").isMissingNode()
                        || node.path("signatureHex").isMissingNode()
                        || node.path("publicKeyHex").isMissingNode()) {
                    return Result.failure(checkpoints.size(), "anchor record is missing required checkpoint fields");
                }
                checkpoints.add(new CheckpointService.Checkpoint(
                        node.path("sequence").asLong(),
                        node.path("chainTipHash").asText(),
                        Instant.parse(node.path("createdAt").asText()),
                        node.path("signatureHex").asText(),
                        node.path("publicKeyHex").asText()));
            }
        } catch (Exception ex) {
            return Result.failure(checkpoints.size(), "external anchor log contains an invalid record");
        }

        if (checkpoints.isEmpty()) return Result.failure(0, "external anchor log contains no checkpoints");

        Map<Long, String> liveHashes = new LinkedHashMap<>();
        liveHashes.put(0L, "0000000000000000000000000000000000000000000000000000000000000000");
        for (AuditEntry entry : liveChain) {
            liveHashes.put(entry.getSequenceNumber(), entry.getCurrentHash());
        }
        long currentSequence = liveChain.size();
        String currentTip = liveChain.isEmpty()
                ? liveHashes.get(0L)
                : liveChain.get(liveChain.size() - 1).getCurrentHash();

        Optional<String> issue = checkpointService.detectTampering(
                currentSequence, currentTip, liveHashes, checkpoints);
        return issue.map(error -> Result.failure(checkpoints.size(), error))
                .orElseGet(() -> Result.success(checkpoints.size()));
    }

    public record Result(boolean valid, int checkpointCount, String error) {
        static Result success(int count) { return new Result(true, count, null); }
        static Result failure(int count, String error) { return new Result(false, count, error); }
    }
}

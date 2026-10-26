package com.auditchain.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional HTTP vision backend. Activated only when auditchain.vision.url is set.
 * On failure, falls back to arithmetic-only analysis (never invents a fraud verdict).
 */
@Component
@Primary
@ConditionalOnProperty(name = "auditchain.vision.url")
public class HttpVisionProvider implements VisionProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpVisionProvider.class);
    private static final Pattern PARTY = Pattern.compile("\"([A-Z]{2,5})\"\\s*:\\s*(\\d+)");

    private final String url;
    private final String apiKey;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public HttpVisionProvider(
            @Value("${auditchain.vision.url}") String url,
            @Value("${auditchain.vision.api-key:}") String apiKey) {
        this.url = url;
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @Override
    public VisionResult analyse(byte[] imageBytes, String declaredPayload) {
        try {
            String body = buildRequestBody(imageBytes, declaredPayload);
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (!apiKey.isBlank()) {
                b.header("Authorization", "Bearer " + apiKey);
            }
            HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return parseProviderResponse(resp.body(), declaredPayload);
            }
            log.warn("Vision provider HTTP {}; falling back to local arithmetic check", resp.statusCode());
        } catch (Exception e) {
            log.warn("Vision provider call failed: {}; falling back", e.toString());
        }
        return localArithmetic(declaredPayload);
    }

    private String buildRequestBody(byte[] imageBytes, String declaredPayload) {
        String img = imageBytes == null || imageBytes.length == 0
                ? "null"
                : "\"" + Base64.getEncoder().encodeToString(imageBytes) + "\"";
        String payload = declaredPayload == null ? "null" : jsonEscape(declaredPayload);
        return "{\"imageBase64\":" + img + ",\"declaredPayload\":" + payload + "}";
    }

    private VisionResult parseProviderResponse(String body, String declaredPayload) {
        VisionResult r = localArithmetic(declaredPayload);
        if (body != null && body.toLowerCase().contains("\"requires_human_review\":true")) {
            r.setRequiresHumanReview(true);
        }
        if (body != null && body.toLowerCase().contains("\"arithmetic_consistent\":false")) {
            r.setArithmeticConsistent(false);
            r.setRequiresHumanReview(true);
        }
        r.setNotes("http-provider; " + (r.getNotes() == null ? "" : r.getNotes()));
        return r;
    }

    private VisionResult localArithmetic(String declaredPayload) {
        VisionResult r = new VisionResult();
        r.setNotes("local arithmetic fallback");
        if (declaredPayload == null || declaredPayload.isBlank()) {
            r.setRequiresHumanReview(true);
            r.setArithmeticConsistent(false);
            r.setNotes("no declared payload; human review required");
            return r;
        }
        int sum = 0;
        Matcher m = PARTY.matcher(declaredPayload);
        while (m.find()) {
            int v = Integer.parseInt(m.group(2));
            r.getPartyVotes().put(m.group(1), v);
            sum += v;
        }
        r.setTotalVotesExtracted(sum);
        int declaredTotal = -1;
        Matcher t = Pattern.compile("\"(?:votes_cast|total_votes)\"\\s*:\\s*(\\d+)").matcher(declaredPayload);
        if (t.find()) declaredTotal = Integer.parseInt(t.group(1));
        boolean consistent = declaredTotal < 0 || declaredTotal == sum;
        r.setArithmeticConsistent(consistent);
        r.setRequiresHumanReview(!consistent);
        if (!consistent) {
            r.setNotes("party sum " + sum + " != declared total " + declaredTotal + "; queued for human review");
        }
        return r;
    }

    private static String jsonEscape(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

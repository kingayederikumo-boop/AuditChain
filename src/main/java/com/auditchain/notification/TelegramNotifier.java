package com.auditchain.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Optional Telegram notification adapter. If credentials are absent, notifications
 * are skipped; Telegram outages never fail the underlying audit workflow.
 */
@Component
public class TelegramNotifier {
    private final String botToken;
    private final String chatId;
    private final ObjectMapper mapper;
    private final HttpClient client;

    public TelegramNotifier(
            @Value("${auditchain.telegram.bot-token:}") String botToken,
            @Value("${auditchain.telegram.chat-id:}") String chatId,
            ObjectMapper mapper) {
        this.botToken = botToken == null ? "" : botToken.trim();
        this.chatId = chatId == null ? "" : chatId.trim();
        this.mapper = mapper;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public boolean isConfigured() {
        return !botToken.isBlank() && !chatId.isBlank();
    }

    public boolean sendMessage(String message) {
        if (!isConfigured() || message == null || message.isBlank()) return false;
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "chat_id", chatId,
                    "text", message,
                    "disable_web_page_preview", true
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) return false;
            JsonNode result = mapper.readTree(response.body());
            return result.path("ok").asBoolean(false);
        } catch (Exception ignored) {
            // Do not leak bot tokens or network response bodies into logs.
            return false;
        }
    }
}

package com.jarvis.aiagent;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.jarvis.aiagent.config.OpenRouterConfig;
import com.jarvis.aiagent.dto.ChatRequest;
import com.jarvis.aiagent.dto.ChatStreamChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles the OpenRouter streaming call and forwards each SSE token
 * to the client via a Spring {@link SseEmitter}.
 *
 * Strategy:
 *  1. Build the OpenRouter request body (OpenAI message format, stream=true).
 *  2. Fire an async HTTP call using Java 11+ HttpClient with line-by-line
 *     body handling.
 *  3. For each "data: ..." SSE line, parse the JSON chunk and emit the
 *     content token immediately — no buffering.
 *  4. On [DONE] or stream end, complete the emitter.
 *  5. On any error, stream a readable error message and complete.
 *
 * No WebFlux / Reactor dependency is needed; the blocking read runs on
 * a cached thread pool so it does not tie up Tomcat workers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentService {

    private static final String SSE_DATA_PREFIX  = "data: ";
    private static final String SSE_DONE_MARKER  = "[DONE]";

    private final OpenRouterConfig config;
    private final ObjectMapper objectMapper;

    // Cached thread pool — each streaming call gets its own thread so it doesn't
    // block Tomcat workers. Compatible with Java 17 (virtual threads need Java 21+).
    private final ExecutorService streamExecutor =
            Executors.newCachedThreadPool();

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Starts an async SSE stream that proxies OpenRouter chunks to the client.
     * Each token is sent as a "message" event; the full assembled text is sent
     * as a final "complete" event before closing.
     */
    public void streamToEmitter(ChatRequest chatRequest, SseEmitter emitter) {
        log.info("[AiAgent] Received streaming chat request, message length={}",
                chatRequest.getMessage() != null ? chatRequest.getMessage().length() : 0);

        streamExecutor.submit(() -> {
            try {
                callOpenRouterStream(chatRequest, emitter);
            } catch (Exception e) {
                log.error("[AiAgent] Unexpected error during streaming", e);
                sendErrorAndComplete(emitter, "Unexpected server error: " + e.getMessage());
            }
        });
    }

    /**
     * Calls OpenRouter, waits for the full response, and returns it as a
     * plain String. Used by the non-streaming /ask endpoint.
     *
     * @throws IOException if the HTTP call fails or the response cannot be read
     */
    public String getFullResponse(ChatRequest chatRequest) throws IOException {
        log.info("[AiAgent] Received blocking chat request, message length={}",
                chatRequest.getMessage() != null ? chatRequest.getMessage().length() : 0);

        String requestBody;
        try {
            requestBody = buildRequestBody(chatRequest);
        } catch (JacksonException e) {
            throw new IOException("Failed to build request body: " + e.getMessage(), e);
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest httpRequest = buildHttpRequest(requestBody);

        log.info("[AiAgent] Calling OpenRouter (blocking), model={}", config.getDefaultModel());

        HttpResponse<java.io.InputStream> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }

        int statusCode = response.statusCode();
        log.info("[AiAgent] OpenRouter responded with HTTP {}", statusCode);

        if (statusCode != 200) {
            String errorBody = readEntireStream(response.body());
            log.error("[AiAgent] OpenRouter error {}: {}", statusCode, errorBody);
            throw new IOException(mapHttpErrorToMessage(statusCode, errorBody));
        }

        return collectFullResponse(response.body());
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private void callOpenRouterStream(ChatRequest chatRequest, SseEmitter emitter) {
        String requestBody;
        try {
            requestBody = buildRequestBody(chatRequest);
        } catch (JacksonException e) {
            log.error("[AiAgent] Failed to serialize request body", e);
            sendErrorAndComplete(emitter, "Failed to build request body");
            return;
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest httpRequest = buildHttpRequest(requestBody);

        log.info("[AiAgent] Calling OpenRouter, model={}", config.getDefaultModel());

        HttpResponse<java.io.InputStream> response;
        try {
            response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            log.error("[AiAgent] HTTP call to OpenRouter failed: {}", e.getMessage());
            sendErrorAndComplete(emitter, "Connection to AI service failed: " + e.getMessage());
            return;
        }

        int statusCode = response.statusCode();
        log.info("[AiAgent] OpenRouter responded with HTTP {}", statusCode);

        if (statusCode != 200) {
            String errorBody = readEntireStream(response.body());
            log.error("[AiAgent] OpenRouter error {}: {}", statusCode, errorBody);
            String friendly = mapHttpErrorToMessage(statusCode, errorBody);
            sendErrorAndComplete(emitter, friendly);
            return;
        }

        log.info("[AiAgent] Streaming response started");
        processStreamLines(response.body(), emitter);
    }

    /** Shared HTTP request builder used by both streaming and blocking paths. */
    private HttpRequest buildHttpRequest(String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl()))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .header("HTTP-Referer", "https://scan-dine.app")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Reads an OpenRouter SSE stream and returns the fully assembled response text.
     * Used by the blocking /ask endpoint.
     */
    private String collectFullResponse(java.io.InputStream inputStream) throws IOException {
        StringBuilder fullResponse = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || !line.startsWith(SSE_DATA_PREFIX)) continue;
                String data = line.substring(SSE_DATA_PREFIX.length()).trim();
                if (SSE_DONE_MARKER.equals(data)) break;
                try {
                    ChatStreamChunk chunk = objectMapper.readValue(data, ChatStreamChunk.class);
                    String token = chunk.getContentToken();
                    if (!token.isEmpty()) fullResponse.append(token);
                } catch (JacksonException e) {
                    log.warn("[AiAgent] Could not parse chunk, skipping: {}", data);
                }
            }
        }
        log.info("[AiAgent] Blocking response collected, length={}", fullResponse.length());
        return fullResponse.toString();
    }

    /**
     * Reads each SSE line from the InputStream and forwards tokens.
     * Also accumulates all tokens and sends a final "complete" event
     * with the full assembled response text.
     */
    private void processStreamLines(java.io.InputStream inputStream, SseEmitter emitter) {
        StringBuilder fullResponse = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                if (!line.startsWith(SSE_DATA_PREFIX)) {
                    continue;
                }

                String data = line.substring(SSE_DATA_PREFIX.length()).trim();

                if (SSE_DONE_MARKER.equals(data)) {
                    log.info("[AiAgent] Stream completed ([DONE] received)");
                    // Send the fully assembled response as a single "complete" event
                    emitter.send(SseEmitter.event().name("complete").data(fullResponse.toString()));
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                    return;
                }

                parseAndEmitChunk(data, emitter, fullResponse);
            }

            // Stream ended without [DONE] — still send complete and close cleanly
            log.info("[AiAgent] Stream ended (EOF without [DONE])");
            emitter.send(SseEmitter.event().name("complete").data(fullResponse.toString()));
            emitter.complete();

        } catch (IOException e) {
            log.error("[AiAgent] Error reading stream: {}", e.getMessage());
            sendErrorAndComplete(emitter, "Stream interrupted: " + e.getMessage());
        }
    }

    private void parseAndEmitChunk(String json, SseEmitter emitter, StringBuilder fullResponse) {
        try {
            ChatStreamChunk chunk = objectMapper.readValue(json, ChatStreamChunk.class);
            String token = chunk.getContentToken();
            if (!token.isEmpty()) {
                fullResponse.append(token);
                emitter.send(SseEmitter.event().name("message").data(token));
            }
        } catch (JacksonException e) {
            // Malformed chunk — log and skip; don't blow up the stream
            log.warn("[AiAgent] Could not parse chunk, skipping: {}", json);
        } catch (IOException e) {
            log.error("[AiAgent] Failed to send SSE event: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Request body builder
    // ---------------------------------------------------------------

    private String buildRequestBody(ChatRequest chatRequest) throws JacksonException {
        List<Map<String, String>> messages = new ArrayList<>();

        // Append prior conversation turns (if any)
        if (chatRequest.getConversationHistory() != null) {
            for (ChatRequest.ConversationMessage turn : chatRequest.getConversationHistory()) {
                messages.add(messageEntry(turn.getRole(), turn.getContent()));
            }
        }

        // Append the latest user message
        messages.add(messageEntry("user", chatRequest.getMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("model",    config.getDefaultModel());
        body.put("messages", messages);
        body.put("stream",   true);

        return objectMapper.writeValueAsString(body);
    }

    private static Map<String, String> messageEntry(String role, String content) {
        Map<String, String> m = new HashMap<>(2);
        m.put("role",    role);
        m.put("content", content);
        return m;
    }

    // ---------------------------------------------------------------
    // Error helpers
    // ---------------------------------------------------------------

    private void sendErrorAndComplete(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException ignored) {
            // Client already disconnected — nothing to do
        } finally {
            emitter.complete();
        }
    }

    private String mapHttpErrorToMessage(int statusCode, String body) {
        return switch (statusCode) {
            case 401 -> "Invalid or missing OpenRouter API key. Check your OPENROUTER_API_KEY environment variable.";
            case 402 -> "OpenRouter account has insufficient credits.";
            case 429 -> "OpenRouter rate limit exceeded. Please try again shortly.";
            case 503 -> "OpenRouter service is temporarily unavailable.";
            default  -> String.format("OpenRouter returned HTTP %d: %s", statusCode, truncate(body, 200));
        };
    }

    private String readEntireStream(java.io.InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (IOException e) {
            return "(could not read error body)";
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}

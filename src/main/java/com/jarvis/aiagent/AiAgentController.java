package com.jarvis.aiagent;

import com.jarvis.aiagent.dto.ChatRequest;
import com.jarvis.aiagent.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * REST controller for the AI agent chat streaming endpoint.
 *
 * Endpoint : POST /api/ai/ask/stream
 * Auth     : public (no JWT required — see SecurityConfig)
 * Response : Server-Sent Events (text/event-stream)
 *
 * ---------------------------------------------------------------
 * CURL TEST EXAMPLE
 * ---------------------------------------------------------------
 * Basic question:
 *
 *   curl -N -X POST http://localhost:8080/dine_in_customer/api/ai/ask/stream \
 *     -H "Content-Type: application/json" \
 *     -d '{"message":"What is the capital of France?"}'
 *
 * With conversation history:
 *
 *   curl -N -X POST http://localhost:8080/dine_in_customer/api/ai/ask/stream \
 *     -H "Content-Type: application/json" \
 *     -d '{
 *       "message": "What is its population?",
 *       "conversationHistory": [
 *         {"role": "user",      "content": "What is the capital of France?"},
 *         {"role": "assistant", "content": "The capital of France is Paris."}
 *       ]
 *     }'
 *
 * The -N flag disables curl buffering so you see each token as it arrives.
 *
 * Expected SSE output (one line per token):
 *   event: message
 *   data: Paris
 *
 *   event: message
 *   data:  is
 *   ...
 *   event: done
 *   data: [DONE]
 *
 * On error (e.g. bad API key):
 *   event: error
 *   data: Invalid or missing OpenRouter API key. Check your OPENROUTER_API_KEY environment variable.
 * ---------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "Streaming chat powered by OpenRouter")
public class AiAgentController {

    /**
     * Timeout for the SSE connection.
     * 5 minutes should comfortably cover any model response; adjust if needed.
     */
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1_000L;

    private final AiAgentService aiAgentService;

    /**
     * POST /api/ai/ask/stream
     *
     * Accepts a user question (and optional conversation history) and streams
     * the AI response back token-by-token as Server-Sent Events.
     *
     * @param chatRequest JSON body with "message" and optional "conversationHistory"
     * @return SseEmitter that pushes chunks as they arrive from OpenRouter
     */
    @Operation(
            summary = "Stream an AI response",
            description = "Sends the user's message to OpenRouter and streams the response " +
                          "back token-by-token via Server-Sent Events.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "SSE stream of AI tokens",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            }
    )
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAiResponse(@RequestBody ChatRequest chatRequest) {
        log.info("[AiAgent] POST /api/ai/ask/stream — message preview: {}",
                truncate(chatRequest.getMessage(), 80));

        if (chatRequest.getMessage() == null || chatRequest.getMessage().isBlank()) {
            // Return an emitter that immediately sends an error and closes
            SseEmitter errorEmitter = new SseEmitter(0L);
            try {
                errorEmitter.send(SseEmitter.event().name("error").data("message must not be blank"));
            } catch (Exception ignored) { /* noop */ }
            errorEmitter.complete();
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // Kick off the async streaming call
        aiAgentService.streamToEmitter(chatRequest, emitter);

        return emitter;
    }

    /**
     * POST /api/ai/ask
     *
     * Same as /ask/stream internally, but waits for the complete response and
     * returns it as plain JSON — no SSE, no streaming on the client side.
     *
     * Use this when you just need the full answer in one shot (e.g. server-side
     * processing, simple REST clients, or when streaming isn't needed).
     *
     * ---------------------------------------------------------------
     * CURL TEST EXAMPLE
     * ---------------------------------------------------------------
     *   curl -X POST http://localhost:8080/dine_in_customer/api/ai/ask \
     *     -H "Content-Type: application/json" \
     *     -d '{"message":"What is the capital of France?"}'
     *
     * Expected response:
     *   {
     *     "message": "What is the capital of France?",
     *     "response": "The capital of France is Paris."
     *   }
     * ---------------------------------------------------------------
     *
     * @param chatRequest JSON body with "message" and optional "conversationHistory"
     * @return {@link ChatResponse} with the full AI answer
     */
    @Operation(
            summary = "Get a complete AI response (non-streaming)",
            description = "Sends the user's message to OpenRouter and waits for the full " +
                          "response before returning it as a single JSON object.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Full AI response",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ChatResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "502", description = "OpenRouter API error")
            }
    )
    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest chatRequest) {
        log.info("[AiAgent] POST /api/ai/ask — message preview: {}",
                truncate(chatRequest.getMessage(), 80));

        if (chatRequest.getMessage() == null || chatRequest.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ChatResponse.builder()
                            .message(chatRequest.getMessage())
                            .response("message must not be blank")
                            .build()
            );
        }

        try {
            String fullResponse = aiAgentService.getFullResponse(chatRequest);
            return ResponseEntity.ok(
                    ChatResponse.builder()
                            .message(chatRequest.getMessage())
                            .response(fullResponse)
                            .build()
            );
        } catch (IOException e) {
            log.error("[AiAgent] Error fetching full response: {}", e.getMessage());
            return ResponseEntity.status(502).body(
                    ChatResponse.builder()
                            .message(chatRequest.getMessage())
                            .response("AI service error: " + e.getMessage())
                            .build()
            );
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String truncate(String s, int max) {
        if (s == null) return "(null)";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}

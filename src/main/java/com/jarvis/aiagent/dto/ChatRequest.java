package com.jarvis.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Inbound request body for POST /api/ai/ask/stream.
 *
 * Example:
 * {
 *   "message": "What is the capital of France?",
 *   "conversationHistory": [
 *     {"role": "user",      "content": "Hello"},
 *     {"role": "assistant", "content": "Hi! How can I help?"}
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    /** The latest user message to send. */
    private String message;

    /**
     * Optional prior turns in OpenAI message format.
     * Each entry must have "role" (user/assistant/system) and "content".
     * If null or empty, only the current message is sent.
     */
    @JsonProperty("conversationHistory")
    private List<ConversationMessage> conversationHistory;

    // ---------------------------------------------------------------
    // Nested: a single turn in conversationHistory
    // ---------------------------------------------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversationMessage {
        private String role;
        private String content;
    }
}

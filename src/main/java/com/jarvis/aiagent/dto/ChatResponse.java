package com.jarvis.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for POST /api/ai/ask (non-streaming endpoint).
 *
 * Example:
 * {
 *   "message": "What is the capital of France?",
 *   "response": "The capital of France is Paris."
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /** The original question echoed back for convenience. */
    private String message;

    /** The fully assembled AI response. */
    private String response;
}

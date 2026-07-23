package com.jarvis.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single SSE data chunk returned by OpenRouter in
 * OpenAI-compatible streaming format.
 *
 * Raw SSE line example:
 *   data: {"id":"...","object":"chat.completion.chunk","choices":[{"delta":{"content":"Hello"},"index":0}]}
 *
 * The stream terminates with:
 *   data: [DONE]
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatStreamChunk {

    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        private int index;
        private Delta delta;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String role;
        private String content;
    }

    /**
     * Convenience: extracts the text token from the first choice delta,
     * returning an empty string when content is absent (e.g. role-only delta).
     */
    public String getContentToken() {
        if (choices == null || choices.isEmpty()) return "";
        Delta delta = choices.get(0).getDelta();
        if (delta == null || delta.getContent() == null) return "";
        return delta.getContent();
    }
}

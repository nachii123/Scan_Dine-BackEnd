package com.jarvis.aiagent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds openrouter.* properties from application.yml.
 *
 * The API key is expected to arrive via the environment variable
 * OPENROUTER_API_KEY, referenced in application.yml as:
 *   openrouter.api-key: ${OPENROUTER_API_KEY}
 *
 * It is never hardcoded here or in any source file.
 */
@Configuration
@ConfigurationProperties(prefix = "openrouter")
@Getter
@Setter
public class OpenRouterConfig {

    /** Bearer token for OpenRouter — injected from OPENROUTER_API_KEY env var. */
    private String apiKey;

    /** Full completions URL, e.g. https://openrouter.ai/api/v1/chat/completions */
    private String baseUrl;

    /** Default model ID, overridable in application.yml. */
    private String defaultModel;
}

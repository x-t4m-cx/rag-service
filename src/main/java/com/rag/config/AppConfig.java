package com.rag.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Root application configuration that enables typed configuration properties.
 */
@Configuration
@EnableConfigurationProperties({RagProperties.class, OpenAiApiProperties.class, WebSearchProperties.class})
public class AppConfig {
}

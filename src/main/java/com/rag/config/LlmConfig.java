package com.rag.config;

import com.rag.websearch.WebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Spring AI {@link ChatClient} with optional tool-calling support.
 */
@Configuration
public class LlmConfig {

    /**
     * Builds a {@link ChatClient} and registers the web search tool when enabled.
     *
     * @param builder        auto-configured chat client builder
     * @param webSearchTool optional web search tool bean
     * @return configured chat client
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ObjectProvider<WebSearchTool> webSearchTool) {
        ChatClient.Builder clientBuilder = builder;
        webSearchTool.ifAvailable(tool -> clientBuilder.defaultTools(tool));
        return clientBuilder.build();
    }
}

package com.rag.llm;

import com.rag.exception.LlmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Stateless wrapper around Spring AI {@link ChatClient} with optional tool-calling support.
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final ChatClient chatClient;

    /**
     * Creates the LLM service.
     *
     * @param chatClient configured Spring AI chat client
     */
    public LlmService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Generates a completion for the given prompt.
     *
     * @param prompt fully constructed prompt
     * @return assistant response text
     */
    public String complete(String prompt) {
        long start = System.currentTimeMillis();
        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("LLM completion finished in {} ms", System.currentTimeMillis() - start);
            return content;
        } catch (Exception exception) {
            throw new LlmException("LLM completion failed: " + exception.getMessage(), exception);
        }
    }

    /**
     * Streams a completion for the given prompt.
     *
     * @param prompt fully constructed prompt
     * @return flux of incremental text chunks
     */
    public Flux<String> stream(String prompt) {
        log.info("Starting LLM streaming completion");
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .filter(chunk -> chunk != null && !chunk.isEmpty())
                .onErrorMap(exception -> new LlmException("LLM streaming failed: " + exception.getMessage(), exception));
    }
}

package com.rag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures the thread pool used for asynchronous document processing.
 */
@Configuration
public class AsyncConfig {

    /**
     * Creates a dedicated executor for document indexing tasks.
     *
     * @param ragProperties RAG configuration including async pool settings
     * @return configured task executor
     */
    @Bean(name = "documentProcessingExecutor")
    public Executor documentProcessingExecutor(RagProperties ragProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ragProperties.async().corePoolSize());
        executor.setMaxPoolSize(ragProperties.async().maxPoolSize());
        executor.setQueueCapacity(ragProperties.async().queueCapacity());
        executor.setThreadNamePrefix("doc-processor-");
        executor.initialize();
        return executor;
    }
}

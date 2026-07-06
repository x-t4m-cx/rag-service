package com.rag.rag;

import com.rag.embedding.EmbeddingService;
import com.rag.llm.LlmService;
import com.rag.vector.VectorMetadataKeys;
import com.rag.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes the RAG pipeline: embed, retrieve, filter, prompt, and generate.
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final PromptBuilderService promptBuilderService;
    private final LlmService llmService;

    /**
     * Creates the RAG service.
     *
     * @param embeddingService     embedding service with cache
     * @param vectorStoreService   vector store adapter
     * @param promptBuilderService prompt builder
     * @param llmService           LLM wrapper
     */
    public RagService(EmbeddingService embeddingService,
                      VectorStoreService vectorStoreService,
                      PromptBuilderService promptBuilderService,
                      LlmService llmService) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.promptBuilderService = promptBuilderService;
        this.llmService = llmService;
    }

    /**
     * Generates a non-streaming answer for the given question.
     *
     * @param question user question
     * @return assistant answer
     */
    public String answer(String question) {
        String prompt = buildPrompt(question);
        return llmService.complete(prompt);
    }

    /**
     * Generates a streaming answer for the given question.
     *
     * @param question user question
     * @return flux of answer chunks
     */
    public Flux<String> answerStream(String question) {
        String prompt = buildPrompt(question);
        return llmService.stream(prompt);
    }

    private String buildPrompt(String question) {
        long start = System.currentTimeMillis();

        log.info("RAG step 1: generating query embedding");
        embeddingService.embed(question);

        log.info("RAG step 2: searching vector store (top-K with similarity threshold)");
        List<Document> documents = vectorStoreService.similaritySearch(question);

        log.info("RAG step 3: {} chunk(s) passed similarity filter", documents.size());
        String context = buildContext(documents);

        log.info("RAG step 4: building prompt");
        String prompt = promptBuilderService.build(context, question);

        log.info("RAG pipeline prepared in {} ms", System.currentTimeMillis() - start);
        return prompt;
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "Контекст не найден.";
        }

        return documents.stream()
                .map(this::formatDocumentSource)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String formatDocumentSource(Document document) {
        Object filename = document.getMetadata().get(VectorMetadataKeys.FILENAME);
        Object chunkIndex = document.getMetadata().get(VectorMetadataKeys.CHUNK_INDEX);
        String source = filename != null ? filename.toString() : "unknown";
        String index = chunkIndex != null ? chunkIndex.toString() : "?";
        return "Источник: " + source + " (chunk " + index + ")\n" + document.getText();
    }
}

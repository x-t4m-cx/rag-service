package com.rag.vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link VectorStoreService} against a real Qdrant instance.
 */
@SpringBootTest(properties = {
        "spring.ai.ollama.api-key=test-key",
        "spring.ai.ollama.base-url=http://localhost:9999",
        "rag.web-search.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class VectorStoreServiceIntegrationTest {

    private static final int EMBEDDING_DIMENSION = 768;

    @Container
    static QdrantContainer qdrant = new QdrantContainer("qdrant/qdrant:v1.13.4");

    @DynamicPropertySource
    static void configureQdrant(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.vectorstore.qdrant.host", qdrant::getHost);
        registry.add("spring.ai.vectorstore.qdrant.port", qdrant::getGrpcPort);
        registry.add("spring.ai.vectorstore.qdrant.collection-name",
                () -> "rag-integration-" + UUID.randomUUID());
    }

    @Autowired
    private VectorStoreService vectorStoreService;

    @MockBean
    private EmbeddingModel embeddingModel;

    @BeforeEach
    void setUpEmbeddings() {
        float[] vector = new float[EMBEDDING_DIMENSION];
        vector[0] = 1.0f;
        when(embeddingModel.embed(anyString())).thenReturn(vector);
        when(embeddingModel.embed(any(Document.class))).thenReturn(vector);
    }

    @Test
    void indexesAndRetrievesDocumentChunks() {
        UUID documentId = UUID.randomUUID();
        Instant uploadedAt = Instant.parse("2026-01-01T00:00:00Z");

        vectorStoreService.indexChunks(
                documentId,
                "integration.txt",
                List.of("Spring AI RAG integration test chunk"),
                uploadedAt
        );

        List<Document> results = vectorStoreService.similaritySearch("RAG integration");

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getMetadata())
                .containsEntry(VectorMetadataKeys.DOCUMENT_ID, documentId.toString())
                .containsEntry(VectorMetadataKeys.FILENAME, "integration.txt");
    }

    @Test
    void deletesDocumentChunksByDocumentId() {
        UUID documentId = UUID.randomUUID();
        Instant uploadedAt = Instant.parse("2026-01-01T00:00:00Z");

        vectorStoreService.indexChunks(
                documentId,
                "delete-me.txt",
                List.of("Chunk to delete"),
                uploadedAt
        );

        vectorStoreService.deleteByDocumentId(documentId);

        List<Document> results = vectorStoreService.similaritySearch("Chunk to delete");
        assertThat(results).isEmpty();
    }
}

package com.rag.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingModel);
    }

    @Test
    void embed_cachesIdenticalTexts() {
        float[] vector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed("hello")).thenReturn(vector);

        float[] first = embeddingService.embed("hello");
        float[] second = embeddingService.embed("  hello  ");

        assertThat(first).isSameAs(vector);
        assertThat(second).isSameAs(vector);
        verify(embeddingModel, times(1)).embed("hello");
    }

    @Test
    void embedAll_reusesCacheAcrossBatch() {
        float[] vector = new float[]{0.5f};
        when(embeddingModel.embed("chunk")).thenReturn(vector);

        List<float[]> embeddings = embeddingService.embedAll(List.of("chunk", "chunk"));

        assertThat(embeddings).hasSize(2);
        assertThat(embeddings.get(0)).isSameAs(vector);
        assertThat(embeddings.get(1)).isSameAs(vector);
        verify(embeddingModel, times(1)).embed("chunk");
    }
}

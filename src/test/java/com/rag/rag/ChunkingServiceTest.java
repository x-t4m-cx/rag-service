package com.rag.rag;

import com.rag.config.RagProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChunkingServiceTest {

    private ChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        RagProperties.AsyncProperties async = new RagProperties.AsyncProperties(4, 8, 100);
        RagProperties properties = new RagProperties(10, 2, 5, 0.7, async);
        chunkingService = new ChunkingService(properties);
    }

    @Test
    void chunk_returnsEmptyListForBlankText() {
        assertThat(chunkingService.chunk("")).isEmpty();
        assertThat(chunkingService.chunk("   ")).isEmpty();
        assertThat(chunkingService.chunk(null)).isEmpty();
    }

    @Test
    void chunk_returnsSingleChunkWhenTextFits() {
        List<String> chunks = chunkingService.chunk("short text");

        assertThat(chunks).containsExactly("short text");
    }

    @Test
    void chunk_splitsLongTextWithOverlap() {
        String text = "0123456789012345678901234567890";
        List<String> chunks = chunkingService.chunk(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.getFirst()).hasSize(10);
        assertThat(String.join("", chunks)).contains("0123456789");
    }

    @Test
    void chunk_throwsWhenOverlapNotSmallerThanChunkSize() {
        RagProperties.AsyncProperties async = new RagProperties.AsyncProperties(4, 8, 100);
        RagProperties invalid = new RagProperties(10, 10, 5, 0.7, async);
        ChunkingService invalidService = new ChunkingService(invalid);

        assertThatThrownBy(() -> invalidService.chunk("012345678901234567890"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlap");
    }
}

package com.rag.websearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WebSearchTool}.
 */
@ExtendWith(MockitoExtension.class)
class WebSearchToolTest {

    @Mock
    private WebSearchService webSearchService;

    @InjectMocks
    private WebSearchTool webSearchTool;

    @Test
    void webSearch_delegatesToService() {
        when(webSearchService.search("Spring AI")).thenReturn("1. Result");

        String result = webSearchTool.webSearch("Spring AI");

        assertThat(result).isEqualTo("1. Result");
        verify(webSearchService).search("Spring AI");
    }
}

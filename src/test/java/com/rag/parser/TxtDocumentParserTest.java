package com.rag.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TxtDocumentParserTest {

    private TxtDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new TxtDocumentParser();
    }

    @Test
    void shouldSupportTxtExtension() {
        assertThat(parser.supports(".txt")).isTrue();
        assertThat(parser.supports(".TXT")).isTrue();
        assertThat(parser.supports(".pdf")).isFalse();
    }

    @Test
    void shouldParsePlainText() throws IOException {
        String content = "Hello, RAG service!";
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = parser.parse(stream);

        assertThat(result).isEqualTo(content);
    }
}

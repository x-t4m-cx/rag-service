package com.rag.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownDocumentParserTest {

    private MarkdownDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new MarkdownDocumentParser();
    }

    @Test
    void shouldSupportMarkdownExtensions() {
        assertThat(parser.supports(".md")).isTrue();
        assertThat(parser.supports(".markdown")).isTrue();
        assertThat(parser.supports(".MD")).isTrue();
        assertThat(parser.supports(".txt")).isFalse();
    }

    @Test
    void shouldParseMarkdownToPlainText() throws IOException {
        String markdown = "# Title\n\nSome **bold** text.";
        ByteArrayInputStream stream = new ByteArrayInputStream(markdown.getBytes(StandardCharsets.UTF_8));

        String result = parser.parse(stream);

        assertThat(result).contains("Title");
        assertThat(result).contains("bold");
        assertThat(result).doesNotContain("**");
    }
}

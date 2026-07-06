package com.rag.parser;

import com.rag.exception.UnsupportedFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentParserRegistryTest {

    private DocumentParserRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DocumentParserRegistry(List.of(
                new TxtDocumentParser(),
                new MarkdownDocumentParser(),
                new PdfDocumentParser(),
                new DocxDocumentParser()
        ));
    }

    @Test
    void shouldParseSupportedFile() throws IOException {
        String content = "Registry test content";
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = registry.parse("notes.txt", stream);

        assertThat(result).isEqualTo(content);
    }

    @Test
    void shouldDetectSupportedExtensions() {
        assertThat(registry.isSupported("document.pdf")).isTrue();
        assertThat(registry.isSupported("readme.MD")).isTrue();
        assertThat(registry.isSupported("report.docx")).isTrue();
        assertThat(registry.isSupported("archive.zip")).isFalse();
    }

    @Test
    void shouldThrowForUnsupportedExtension() {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> registry.parse("image.png", stream))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining(".png");
    }
}

package com.rag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DocxDocumentParserTest {

    private DocxDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new DocxDocumentParser();
    }

    @Test
    void shouldSupportDocxExtension() {
        assertThat(parser.supports(".docx")).isTrue();
        assertThat(parser.supports(".DOCX")).isTrue();
        assertThat(parser.supports(".doc")).isFalse();
    }

    @Test
    void shouldParseDocxContent() throws IOException {
        byte[] docxBytes = createSampleDocx("First paragraph", "Second paragraph");
        ByteArrayInputStream stream = new ByteArrayInputStream(docxBytes);

        String result = parser.parse(stream);

        assertThat(result).contains("First paragraph");
        assertThat(result).contains("Second paragraph");
    }

    private byte[] createSampleDocx(String... paragraphs) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (String text : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(text);
            }
            document.write(output);
            return output.toByteArray();
        }
    }
}

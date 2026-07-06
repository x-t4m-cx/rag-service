package com.rag.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PdfDocumentParserTest {

    private PdfDocumentParser parser;

    @BeforeEach
    void setUp() {
        parser = new PdfDocumentParser();
    }

    @Test
    void shouldSupportPdfExtension() {
        assertThat(parser.supports(".pdf")).isTrue();
        assertThat(parser.supports(".PDF")).isTrue();
        assertThat(parser.supports(".docx")).isFalse();
    }

    @Test
    void shouldParsePdfContent() throws IOException {
        byte[] pdfBytes = createSamplePdf("PDF parser test content");
        ByteArrayInputStream stream = new ByteArrayInputStream(pdfBytes);

        String result = parser.parse(stream);

        assertThat(result).contains("PDF parser test content");
    }

    private byte[] createSamplePdf(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}

package com.rag.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileExtensionUtilsTest {

    @Test
    void shouldExtractLowercaseExtension() {
        assertThat(FileExtensionUtils.extractExtension("Report.PDF")).isEqualTo(".pdf");
        assertThat(FileExtensionUtils.extractExtension("README.md")).isEqualTo(".md");
    }

    @Test
    void shouldReturnEmptyWhenExtensionMissing() {
        assertThat(FileExtensionUtils.extractExtension("README")).isEmpty();
        assertThat(FileExtensionUtils.extractExtension("")).isEmpty();
        assertThat(FileExtensionUtils.extractExtension(null)).isEmpty();
    }
}

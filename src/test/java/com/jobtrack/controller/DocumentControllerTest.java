package com.jobtrack.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentControllerTest {

    @Test
    void plainFilenameIsQuoted() {
        assertThat(DocumentController.attachmentHeader("Senior_Resume.pdf"))
                .isEqualTo("attachment; filename=\"Senior_Resume.pdf\"");
    }

    @Test
    void quotesAndNewlinesCannotBreakOutOfHeader() {
        String header = DocumentController.attachmentHeader("evil\"; x=\"y\r\nSet-Cookie: a=b.pdf");

        assertThat(header).doesNotContain("\r").doesNotContain("\n");
        assertThat(header).doesNotContain("evil\"; x=");
    }

    @Test
    void nonAsciiFilenameIsEncoded() {
        assertThat(DocumentController.attachmentHeader("résumé.pdf")).contains("filename*=UTF-8''");
    }

    @Test
    void blankFilenameFallsBackToDefault() {
        assertThat(DocumentController.attachmentHeader("  ")).contains("document");
    }
}

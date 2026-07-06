package com.rag.exception;

import com.rag.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/documents/123");
    }

    @Test
    void shouldReturn404ForDocumentNotFound() {
        UUID id = UUID.randomUUID();
        DocumentNotFoundException ex = new DocumentNotFoundException(id);

        ResponseEntity<ErrorResponse> response = handler.handleDocumentNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains(id.toString());
        assertThat(response.getBody().path()).isEqualTo("/api/documents/123");
    }

    @Test
    void shouldReturn400ForUnsupportedFileType() {
        UnsupportedFileTypeException ex = new UnsupportedFileTypeException(".exe");

        ResponseEntity<ErrorResponse> response = handler.handleUnsupportedFileType(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains(".exe");
    }

    @Test
    void shouldReturn413ForMaxUploadSize() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1);

        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSize(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("50 MB");
    }
}

package com.jobtrack.exception;

import com.jobtrack.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/applications");
        return request;
    }

    @Test
    void unexpectedExceptionDoesNotLeakInternalMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new IllegalStateException("could not execute statement; SQL [insert into users ...]"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected server error occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/applications");
    }

    @Test
    void malformedBodyIsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void typeMismatchNamesParameterAndValue() throws Exception {
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sample", int.class), 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "page", parameter, new NumberFormatException());

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("page").contains("abc").contains("Integer");
    }

    @Test
    void missingParameterIsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(
                new MissingServletRequestParameterException("type", "DocumentType"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("'type'");
    }

    @Test
    void oversizedUploadIsPayloadTooLarge() {
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSize(
                new MaxUploadSizeExceededException(15L * 1024 * 1024), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody().getStatus()).isEqualTo(413);
    }

    @SuppressWarnings("unused")
    private void sample(int page) {
    }
}

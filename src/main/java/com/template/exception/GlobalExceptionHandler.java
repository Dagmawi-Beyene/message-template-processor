package com.template.exception;

import com.template.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateDownloadException.class)
    public ResponseEntity<ErrorResponse> handleTemplateDownloadException(TemplateDownloadException e) {
        log.error("Template download error", e);
        return createErrorResponse(HttpStatus.BAD_GATEWAY, "Failed to download template", e);
    }

    @ExceptionHandler(TemplateProcessingException.class)
    public ResponseEntity<ErrorResponse> handleTemplateProcessingException(TemplateProcessingException e) {
        log.error("Template processing error", e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Failed to process template", e);
    }

    @ExceptionHandler(TemplatePersistenceException.class)
    public ResponseEntity<ErrorResponse> handleTemplatePersistenceException(TemplatePersistenceException e) {
        log.error("Template persistence error", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save template", e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message, Exception e) {
        ErrorResponse response = new ErrorResponse(
            status.value(),
            message,
            e.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }
} 
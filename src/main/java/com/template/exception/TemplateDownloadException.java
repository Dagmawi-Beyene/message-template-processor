package com.template.exception;

public class TemplateDownloadException extends RuntimeException {
    public TemplateDownloadException(String message) {
        super(message);
    }

    public TemplateDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
} 
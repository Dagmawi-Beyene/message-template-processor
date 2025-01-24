package com.template.exception;

public class TemplatePersistenceException extends RuntimeException {
    public TemplatePersistenceException(String message) {
        super(message);
    }

    public TemplatePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
} 
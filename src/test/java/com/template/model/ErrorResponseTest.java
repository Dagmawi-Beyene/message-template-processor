package com.template.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructor_Success() {
        // Given
        int status = 400;
        String message = "Bad Request";
        String detail = "Invalid input";

        // When
        ErrorResponse response = new ErrorResponse(status, message, detail);

        // Then
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(detail, response.getDetail());
    }
} 
package com.template.model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class MessageTemplateTest {

    @Test
    void builder_Success() {
        // Given
        String id = "test-id";
        String key = "TEST_KEY";
        String name = "Test Template";
        TrafficType trafficType = TrafficType.builder()
            .key("LONG_DISTANCE")
            .name("Kaukoliikenne")
            .build();
        String subject = "Test Subject";
        String body = "Test Body";
        List<String> parameters = List.of("PARAM1", "PARAM2");

        // When
        MessageTemplate template = MessageTemplate.builder()
            .id(id)
            .key(key)
            .name(name)
            .trafficType(trafficType)
            .subject(subject)
            .body(body)
            .parameters(parameters)
            .build();

        // Then
        assertEquals(id, template.getId());
        assertEquals(key, template.getKey());
        assertEquals(name, template.getName());
        assertEquals(trafficType, template.getTrafficType());
        assertEquals(subject, template.getSubject());
        assertEquals(body, template.getBody());
        assertEquals(parameters, template.getParameters());
    }
} 
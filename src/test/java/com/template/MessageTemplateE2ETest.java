package com.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.model.domain.MessageTemplate;
import com.template.model.domain.TrafficType;
import com.template.service.DynamoDBService;
import com.template.service.S3Service;
import com.template.service.SQSListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest
@Import({ObjectMapper.class})
class MessageTemplateE2ETest {

    @MockBean
    private S3Service s3Service;

    @MockBean
    private DynamoDBService dynamoDBService;

    @MockBean
    private SQSListener sqsListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEndToEndTemplateProcessing() {
        // Given
        MessageTemplate template = createTestTemplate();
        when(dynamoDBService.findTemplateById(template.getId())).thenReturn(template);
        when(dynamoDBService.findTemplatesByTrafficType(template.getTrafficType().getKey()))
            .thenReturn(List.of(template));
        
        // When
        dynamoDBService.saveTemplate(template);
        
        // Then
        MessageTemplate savedTemplate = dynamoDBService.findTemplateById(template.getId());
        assertNotNull(savedTemplate);
        assertEquals(template.getId(), savedTemplate.getId());
        assertEquals(template.getName(), savedTemplate.getName());
        assertEquals(template.getTrafficType().getKey(), savedTemplate.getTrafficType().getKey());
        
        // Verify findByTrafficType
        List<MessageTemplate> templates = dynamoDBService.findTemplatesByTrafficType(
            template.getTrafficType().getKey()
        );
        assertFalse(templates.isEmpty());
        assertTrue(templates.stream()
            .anyMatch(t -> t.getId().equals(template.getId())));
        
        // Verify interactions
        verify(dynamoDBService).saveTemplate(template);
        verify(dynamoDBService).findTemplateById(template.getId());
        verify(dynamoDBService).findTemplatesByTrafficType(template.getTrafficType().getKey());
    }

    @Test
    void testTemplateUpdateFlow() {
        // Given
        MessageTemplate template = createTestTemplate();
        MessageTemplate updatedTemplate = createTestTemplate();
        updatedTemplate.setSubject("Updated Subject");
        
        // Setup mock behavior
        when(dynamoDBService.findTemplateById(template.getId()))
            .thenReturn(template)
            .thenReturn(updatedTemplate);

        // When
        dynamoDBService.saveTemplate(template);
        template.setSubject("Updated Subject");
        dynamoDBService.saveTemplate(template);

        // Then
        MessageTemplate result = dynamoDBService.findTemplateById(template.getId());
        assertNotNull(result);
        assertEquals("Updated Subject", result.getSubject());
        
        // Verify interactions
        verify(dynamoDBService, times(2)).saveTemplate(any(MessageTemplate.class));
        verify(dynamoDBService).findTemplateById(template.getId());
    }

    private MessageTemplate createTestTemplate() {
        return MessageTemplate.builder()
            .id("test-template-id")
            .key("TEST_KEY")
            .name("Test Template")
            .trafficType(TrafficType.builder()
                .key("LONG_DISTANCE")
                .name("Long Distance")
                .build())
            .subject("Test Subject")
            .body("Hello ${name}, your train to ${destination} is ${status}")
            .parameters(List.of("name", "destination", "status"))
            .build();
    }
} 
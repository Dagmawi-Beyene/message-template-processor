package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.model.domain.MessageTemplate;
import com.template.model.domain.TrafficType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamoDBServiceTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    private DynamoDBService dynamoDBService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        dynamoDBService = new DynamoDBService(dynamoDbClient, objectMapper);
    }

    @Test
    void saveTemplate_Success() {
        // Given
        MessageTemplate template = createTestTemplate();
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
            .thenReturn(PutItemResponse.builder().build());

        // When
        dynamoDBService.saveTemplate(template);

        // Then
        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void findTemplateById_Success() {
        // Given
        String templateId = "test-id";
        Map<String, AttributeValue> item = createTestItem();
        
        QueryResponse queryResponse = QueryResponse.builder()
            .items(List.of(item))
            .build();
            
        when(dynamoDbClient.query(any(QueryRequest.class)))
            .thenReturn(queryResponse);

        // When
        MessageTemplate result = dynamoDBService.findTemplateById(templateId);

        // Then
        assertNotNull(result);
        assertEquals(templateId, result.getId());
        assertEquals("TEST_KEY", result.getKey());
        assertEquals("LONG_DISTANCE", result.getTrafficType().getKey());
    }

    @Test
    void findTemplatesByTrafficType_Success() {
        // Given
        String trafficType = "LONG_DISTANCE";
        Map<String, AttributeValue> item = createTestItem();
        
        QueryResponse queryResponse = QueryResponse.builder()
            .items(List.of(item))
            .build();
            
        when(dynamoDbClient.query(any(QueryRequest.class)))
            .thenReturn(queryResponse);

        // When
        List<MessageTemplate> results = dynamoDBService.findTemplatesByTrafficType(trafficType);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(trafficType, results.get(0).getTrafficType().getKey());
    }

    @Test
    void saveTemplate_whenNullFields_shouldHandleGracefully() {
        // Test null field handling
        MessageTemplate template = MessageTemplate.builder()
            .id("test-id")
            .key("test-key")  // Required fields
            .name("test-name")
            .trafficType(TrafficType.builder()
                .key("SMS")
                .name("SMS")
                .build())
            .subject("test-subject")
            .body("test-body")
            .parameters(Collections.emptyList())
            .build();
        
        dynamoDBService.saveTemplate(template);
        
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        Map<String, AttributeValue> items = putItemRequestCaptor.getValue().item();
        assertNotNull(items.get("id"));
    }

    @Test
    void findTemplatesByTrafficType_whenNoTemplatesExist_shouldReturnEmptyList() {
        // Setup
        QueryResponse mockResponse = QueryResponse.builder()
            .items(Collections.emptyList())
            .build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(mockResponse);
        
        // Execute
        List<MessageTemplate> results = dynamoDBService.findTemplatesByTrafficType("SMS");
        
        // Verify
        assertTrue(results.isEmpty());
        verify(dynamoDbClient).query(any(QueryRequest.class));
    }

    @Test
    void findTemplateById_whenTemplateDoesNotExist_shouldReturnNull() {
        // Setup
        QueryResponse mockResponse = QueryResponse.builder()
            .items(Collections.emptyList())
            .build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(mockResponse);
        
        // Execute
        MessageTemplate result = dynamoDBService.findTemplateById("non-existent-id");
        
        // Verify
        assertNull(result);
        verify(dynamoDbClient).query(any(QueryRequest.class));
    }

    private MessageTemplate createTestTemplate() {
        return MessageTemplate.builder()
            .id("test-id")
            .key("TEST_KEY")
            .name("Test Template")
            .trafficType(TrafficType.builder()
                .key("LONG_DISTANCE")
                .name("Long Distance")
                .build())
            .subject("Test Subject")
            .body("Test Body")
            .parameters(List.of("PARAM1", "PARAM2"))
            .build();
    }

    private Map<String, AttributeValue> createTestItem() {
        return Map.of(
            "id", AttributeValue.builder().s("test-id").build(),
            "key", AttributeValue.builder().s("TEST_KEY").build(),
            "name", AttributeValue.builder().s("Test Template").build(),
            "trafficTypeKey", AttributeValue.builder().s("LONG_DISTANCE").build(),
            "trafficTypeName", AttributeValue.builder().s("Long Distance").build(),
            "subject", AttributeValue.builder().s("Test Subject").build(),
            "body", AttributeValue.builder().s("Test Body").build(),
            "parameters", AttributeValue.builder().ss("PARAM1", "PARAM2").build()
        );
    }
} 
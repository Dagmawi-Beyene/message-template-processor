package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.model.domain.MessageTemplate;
import com.template.model.domain.TrafficType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.template.exception.TemplatePersistenceException;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDBService {
    private static final String TABLE_NAME = "dynamodb-table";
    private static final String PK_PREFIX = "TEMPLATE#";
    private static final String SK_PREFIX = "METADATA#";
    private static final String GSI_PK_PREFIX = "TRAFFICTYPE#";

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    /**
     * Saves or updates a template in DynamoDB
     * 
     * @param template The template to save
     */
    public void saveTemplate(MessageTemplate template) {
        try {
            log.info("Saving template with ID: {}", template.getId());

            Map<String, AttributeValue> item = new HashMap<>();

            // Primary key attributes
            item.put("PK", AttributeValue.builder().s(PK_PREFIX + template.getId()).build());

            // Handle null traffic type for sort key
            String skValue = template.getTrafficType() != null ? SK_PREFIX + template.getTrafficType().getKey()
                    : SK_PREFIX + "DEFAULT";
            item.put("SK", AttributeValue.builder().s(skValue).build());

            // GSI attributes only if traffic type exists
            if (template.getTrafficType() != null) {
                item.put("GSI_PK", AttributeValue.builder()
                        .s(GSI_PK_PREFIX + template.getTrafficType().getKey())
                        .build());
                item.put("GSI_SK", AttributeValue.builder()
                        .s(PK_PREFIX + template.getId())
                        .build());

                // Traffic type attributes
                item.put("trafficTypeKey", AttributeValue.builder()
                        .s(template.getTrafficType().getKey())
                        .build());
                item.put("trafficTypeName", AttributeValue.builder()
                        .s(template.getTrafficType().getName())
                        .build());
            }

            // Template attributes
            item.put("id", AttributeValue.builder().s(template.getId()).build());
            item.put("key", AttributeValue.builder().s(template.getKey()).build());
            item.put("name", AttributeValue.builder().s(template.getName()).build());
            item.put("subject", AttributeValue.builder().s(template.getSubject()).build());
            item.put("body", AttributeValue.builder().s(template.getBody()).build());

            // Parameters as string set
            if (template.getParameters() != null && !template.getParameters().isEmpty()) {
                item.put("parameters", AttributeValue.builder()
                        .ss(template.getParameters())
                        .build());
            }

            PutItemRequest putRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putRequest);

            log.info("Successfully saved template with ID: {}", template.getId());

        } catch (Exception e) {
            log.error("Error saving template with ID: {}", template.getId(), e);
            throw new TemplatePersistenceException("Failed to save template to DynamoDB", e);
        }
    }

    /**
     * Finds a template by ID
     * 
     * @param templateId The template ID
     * @return The found template or null if not found
     */
    public MessageTemplate findTemplateById(String templateId) {
        try {
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .keyConditionExpression("PK = :pk")
                    .expressionAttributeValues(Map.of(
                            ":pk", AttributeValue.builder().s(PK_PREFIX + templateId).build()))
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);

            return response.items().stream()
                    .findFirst()
                    .map(this::mapToTemplate)
                    .orElse(null);

        } catch (Exception e) {
            log.error("Error finding template with ID: {}", templateId, e);
            throw new TemplatePersistenceException("Failed to query template from DynamoDB", e);
        }
    }

    /**
     * Finds all templates by traffic type
     * 
     * @param trafficType The traffic type key
     * @return List of templates for the given traffic type
     */
    public List<MessageTemplate> findTemplatesByTrafficType(String trafficType) {
        try {
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .indexName("GSI_TrafficType")
                    .keyConditionExpression("GSI_PK = :pk")
                    .expressionAttributeValues(Map.of(
                            ":pk", AttributeValue.builder().s(GSI_PK_PREFIX + trafficType).build()))
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);

            return response.items().stream()
                    .map(this::mapToTemplate)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding templates for traffic type: {}", trafficType, e);
            throw new TemplatePersistenceException("Failed to query templates from DynamoDB", e);
        }
    }

    private MessageTemplate mapToTemplate(Map<String, AttributeValue> item) {
        var templateBuilder = MessageTemplate.builder()
                .id(item.get("id").s())
                .key(item.get("key").s())
                .name(item.get("name").s())
                .subject(item.get("subject").s())
                .body(item.get("body").s());

        // Add traffic type if it exists
        if (item.containsKey("trafficTypeKey") && item.containsKey("trafficTypeName")) {
            var trafficType = TrafficType.builder()
                    .key(item.get("trafficTypeKey").s())
                    .name(item.get("trafficTypeName").s())
                    .build();
            templateBuilder.trafficType(trafficType);
        }

        // Add parameters if they exist
        if (item.containsKey("parameters")) {
            templateBuilder.parameters(item.get("parameters").ss());
        }

        return templateBuilder.build();
    }
}
package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.model.aws.S3EventNotification;
import com.template.model.cms.CmsResponse;
import com.template.model.domain.MessageTemplate;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSListenerTest {

    @Mock
    private S3Service s3Service;
    
    @Mock
    private TemplateTransformer templateTransformer;
    
    @Mock
    private DynamoDBService dynamoDBService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private Acknowledgement acknowledgement;

    private SQSListener sqsListener;

    @BeforeEach
    void setUp() {
        sqsListener = new SQSListener(s3Service, templateTransformer, dynamoDBService, objectMapper);
    }

    @Test
    void handleS3Event_Success() throws Exception {
        // Given
        String message = "test-message";
        String bucket = "test-bucket";
        String key = "test/template.json";
        
        S3EventNotification event = createTestEvent(bucket, key);
        CmsResponse cmsResponse = new CmsResponse();
        MessageTemplate template = MessageTemplate.builder().build();
        
        when(objectMapper.readValue(eq(message), eq(S3EventNotification.class)))
            .thenReturn(event);
        when(s3Service.downloadTemplate(bucket, key))
            .thenReturn(cmsResponse);
        when(templateTransformer.transform(cmsResponse))
            .thenReturn(template);
        doNothing().when(dynamoDBService).saveTemplate(template);
        doNothing().when(acknowledgement).acknowledge();
        
        // When
        sqsListener.handleS3Event(message, acknowledgement);
        
        // Then
        verify(s3Service).downloadTemplate(bucket, key);
        verify(templateTransformer).transform(cmsResponse);
        verify(dynamoDBService).saveTemplate(template);
        verify(acknowledgement).acknowledge();
    }

    @Test
    void handleS3Event_ErrorHandling() throws Exception {
        // Given
        String message = "test-message";
        String bucket = "test-bucket";
        String key = "test/template.json";
        
        S3EventNotification event = createTestEvent(bucket, key);
        
        when(objectMapper.readValue(eq(message), eq(S3EventNotification.class)))
            .thenReturn(event);
        when(s3Service.downloadTemplate(any(), any()))
            .thenThrow(new RuntimeException("Test error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> sqsListener.handleS3Event(message, acknowledgement));
        verify(acknowledgement, never()).acknowledge();
    }

    private S3EventNotification createTestEvent(String bucket, String key) {
        S3EventNotification.S3BucketEntity bucketEntity = new S3EventNotification.S3BucketEntity();
        bucketEntity.setName(bucket);

        S3EventNotification.S3ObjectEntity objectEntity = new S3EventNotification.S3ObjectEntity();
        objectEntity.setKey(key);

        S3EventNotification.S3Entity s3Entity = new S3EventNotification.S3Entity();
        s3Entity.setBucket(bucketEntity);
        s3Entity.setObject(objectEntity);

        S3EventNotification.S3EventNotificationRecord record = new S3EventNotification.S3EventNotificationRecord();
        record.setS3(s3Entity);

        S3EventNotification event = new S3EventNotification();
        event.setRecords(List.of(record));

        return event;
    }
} 
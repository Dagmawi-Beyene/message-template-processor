package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.template.model.aws.S3EventNotification;
import com.template.model.cms.Template;
import com.template.model.cms.CmsResponse;
import com.template.model.domain.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.annotation.SqsListener;
import com.template.exception.TemplateProcessingException;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSListener {
    private final S3Service s3Service;
    private final TemplateTransformer templateTransformer;
    private final DynamoDBService dynamoDBService;
    private final ObjectMapper objectMapper;

    /**
     * Listens for S3 event notifications from SQS
     * Uses SQS FIFO queue with deduplication enabled for exactly-once processing
     * 
     * @param message The SQS message containing S3 event notification
     */
    @SqsListener(value = "${aws.sqs.queue-name}", factory = "defaultSqsListenerContainerFactory")
    public void handleS3Event(String message, Acknowledgement acknowledgement) {
        try {
            log.info("Received SQS message: {}", message);

            log.debug("Attempting to parse S3 event notification");
            S3EventNotification event = objectMapper.readValue(message, S3EventNotification.class);
            log.debug("Successfully parsed S3 event notification: {}", event);

            // Add null check for records
            if (event.getRecords() == null) {
                log.error("No records found in S3 event notification");
                // Try parsing the raw message to get the S3 details
                JsonNode jsonNode = objectMapper.readTree(message);
                JsonNode records = jsonNode.get("Records");
                if (records != null && records.isArray() && records.size() > 0) {
                    JsonNode s3Node = records.get(0).get("s3");
                    if (s3Node != null) {
                        String bucket = s3Node.get("bucket").get("name").asText();
                        String key = s3Node.get("object").get("key").asText();
                        processS3Object(bucket, key);
                    }
                }
            } else {
                for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
                    log.debug("Processing record: {}", record);
                    processRecord(record);
                }
            }

            acknowledgement.acknowledge();
            log.info("Message successfully processed and acknowledged");

        } catch (Exception e) {
            log.error("Error processing SQS message: {}", message, e);
            log.error("Full exception details:", e);
            throw new TemplateProcessingException("Failed to process SQS message", e);
        }
    }

    private void processRecord(S3EventNotification.S3EventNotificationRecord record) {
        try {
            String bucket = record.getS3().getBucket().getName();
            String key = URLDecoder.decode(record.getS3().getObject().getKey(), StandardCharsets.UTF_8);
            processS3Template(bucket, key);
        } catch (Exception e) {
            log.error("Error processing record: {}", e.getMessage(), e);
            log.error("Full exception details:", e);
            throw new RuntimeException("Failed to process S3 event", e);
        }
    }

    private void processS3Object(String bucket, String key) {
        try {
            processS3Template(bucket, key);
        } catch (Exception e) {
            log.error("Error processing S3 object: {}", e.getMessage(), e);
            log.error("Full exception details:", e);
            throw new RuntimeException("Failed to process S3 object", e);
        }
    }

    /**
     * Common method to process S3 template from bucket and key
     * 
     * @param bucket S3 bucket name
     * @param key S3 object key
     */
    private void processS3Template(String bucket, String key) {
        log.info("Processing S3 template - Bucket: {}, Key: {}", bucket, key);
        
        log.debug("Downloading template from S3");
        CmsResponse cmsResponse = s3Service.downloadTemplate(bucket, key);
        log.debug("Successfully downloaded template: {}", cmsResponse);
        
        log.debug("Transforming template");
        MessageTemplate domainTemplate = templateTransformer.transform(cmsResponse);
        log.debug("Template transformed: {}", domainTemplate);
        
        if (domainTemplate != null) {
            log.debug("Saving template to DynamoDB");
            dynamoDBService.saveTemplate(domainTemplate);
            log.info("Successfully saved template to DynamoDB");
        } else {
            log.warn("No template was transformed, skipping save");
        }
        
        log.info("Successfully processed template from bucket: {}, key: {}", bucket, key);
    }
}
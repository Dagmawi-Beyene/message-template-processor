package com.template.model.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class S3EventNotificationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testS3EventNotificationDeserialization() throws Exception {
        String json = """
        {
            "records": [
                {
                    "messageId": "message-1",
                    "s3": {
                        "bucket": {
                            "name": "test-bucket"
                        },
                        "object": {
                            "key": "test/file.json"
                        }
                    }
                }
            ]
        }
        """;

        S3EventNotification notification = objectMapper.readValue(json, S3EventNotification.class);
        
        assertNotNull(notification);
        assertEquals(1, notification.getRecords().size());
        
        S3EventNotification.S3EventNotificationRecord record = notification.getRecords().get(0);
        assertEquals("message-1", record.getMessageId());
        assertEquals("test-bucket", record.getS3().getBucket().getName());
        assertEquals("test/file.json", record.getS3().getObject().getKey());
    }

    @Test
    void testMultipleRecords() {
        S3EventNotification notification = new S3EventNotification();
        
        S3EventNotification.S3EventNotificationRecord record1 = createRecord("msg1", "bucket1", "key1");
        S3EventNotification.S3EventNotificationRecord record2 = createRecord("msg2", "bucket2", "key2");
        
        notification.setRecords(Arrays.asList(record1, record2));
        
        assertEquals(2, notification.getRecords().size());
        assertEquals("msg1", notification.getRecords().get(0).getMessageId());
        assertEquals("msg2", notification.getRecords().get(1).getMessageId());
    }

    @Test
    void testEqualsAndHashCode() {
        S3EventNotification notification1 = new S3EventNotification();
        S3EventNotification notification2 = new S3EventNotification();
        
        List<S3EventNotification.S3EventNotificationRecord> records = 
            Arrays.asList(createRecord("msg1", "bucket1", "key1"));
        
        notification1.setRecords(records);
        notification2.setRecords(records);
        
        assertEquals(notification1, notification2);
        assertEquals(notification1.hashCode(), notification2.hashCode());
    }

    @Test
    void testNestedEqualsAndHashCode() {
        S3EventNotification.S3BucketEntity bucket1 = new S3EventNotification.S3BucketEntity();
        bucket1.setName("test-bucket");
        
        S3EventNotification.S3BucketEntity bucket2 = new S3EventNotification.S3BucketEntity();
        bucket2.setName("test-bucket");
        
        assertEquals(bucket1, bucket2);
        assertEquals(bucket1.hashCode(), bucket2.hashCode());
    }

    @Test
    void testNullHandling() {
        S3EventNotification notification = new S3EventNotification();
        assertNull(notification.getRecords());
        
        S3EventNotification.S3EventNotificationRecord record = new S3EventNotification.S3EventNotificationRecord();
        assertNull(record.getMessageId());
        assertNull(record.getS3());
        
        S3EventNotification.S3Entity s3 = new S3EventNotification.S3Entity();
        assertNull(s3.getBucket());
        assertNull(s3.getObject());
    }

    private S3EventNotification.S3EventNotificationRecord createRecord(String messageId, String bucketName, String key) {
        S3EventNotification.S3EventNotificationRecord record = new S3EventNotification.S3EventNotificationRecord();
        record.setMessageId(messageId);
        
        S3EventNotification.S3Entity s3 = new S3EventNotification.S3Entity();
        
        S3EventNotification.S3BucketEntity bucket = new S3EventNotification.S3BucketEntity();
        bucket.setName(bucketName);
        
        S3EventNotification.S3ObjectEntity object = new S3EventNotification.S3ObjectEntity();
        object.setKey(key);
        
        s3.setBucket(bucket);
        s3.setObject(object);
        record.setS3(s3);
        
        return record;
    }
}
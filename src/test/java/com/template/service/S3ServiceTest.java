package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.exception.TemplateDownloadException;
import com.template.model.cms.CmsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectMapper objectMapper;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, objectMapper);
    }

    @Test
    void downloadTemplate_Success() throws IOException {
        // Given
        String bucket = "test-bucket";
        String key = "test/template.json";
        CmsResponse expectedResponse = new CmsResponse();
        
        ResponseInputStream<GetObjectResponse> s3Response = 
            new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream("{}".getBytes())
            );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Response);
        when(objectMapper.readValue(any(ResponseInputStream.class), eq(CmsResponse.class)))
            .thenReturn(expectedResponse);

        // When
        CmsResponse result = s3Service.downloadTemplate(bucket, key);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(objectMapper).readValue(any(ResponseInputStream.class), eq(CmsResponse.class));
    }

    @Test
    void downloadTemplate_S3Exception() {
        // Given
        String bucket = "test-bucket";
        String key = "test/template.json";
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(S3Exception.builder().build());

        // When/Then
        assertThrows(TemplateDownloadException.class, () -> 
            s3Service.downloadTemplate(bucket, key));
    }

    @Test
    void downloadTemplate_DeserializationException() throws IOException {
        // Given
        String bucket = "test-bucket";
        String key = "test/template.json";
        
        ResponseInputStream<GetObjectResponse> s3Response = 
            new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream("{}".getBytes())
            );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Response);
        when(objectMapper.readValue(any(ResponseInputStream.class), eq(CmsResponse.class)))
            .thenThrow(new IOException("Failed to parse"));

        // When/Then
        assertThrows(TemplateDownloadException.class, () -> 
            s3Service.downloadTemplate(bucket, key));
    }
}
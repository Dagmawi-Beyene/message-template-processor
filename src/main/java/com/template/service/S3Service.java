package com.template.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.exception.TemplateDownloadException;
import com.template.model.cms.Template;
import com.template.model.cms.CmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    /**
     * Downloads and deserializes a template from S3
     * 
     * @param bucket S3 bucket name
     * @param key    Object key in S3
     * @return Deserialized Template object
     * @throws TemplateDownloadException if download or deserialization fails
     */
    public CmsResponse downloadTemplate(String bucket, String key) {
        try {
            log.debug("Downloading template from S3 - bucket: {}, key: {}", bucket, key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            return objectMapper.readValue(response, CmsResponse.class);

        } catch (Exception e) {
            log.error("Failed to download template from S3 - bucket: {}, key: {}", bucket, key, e);
            throw new TemplateDownloadException("Failed to download template from S3", e);
        }
    }
}
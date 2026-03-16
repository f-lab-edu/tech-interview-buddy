package com.example.tech_interview_buddy.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3RequestFactory {

    private final AwsS3Properties properties;

    public PutObjectRequest putRequest(String key, String contentType, long contentLength) {
        return PutObjectRequest.builder()
            .bucket(properties.getBucketName())
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build();
    }

    public DeleteObjectRequest deleteRequest(String key) {
        return DeleteObjectRequest.builder()
            .bucket(properties.getBucketName())
            .key(key)
            .build();
    }
}

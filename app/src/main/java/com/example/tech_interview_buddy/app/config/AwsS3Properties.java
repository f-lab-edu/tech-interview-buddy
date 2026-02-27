package com.example.tech_interview_buddy.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    private String bucketName;
    private String region;
    private String accessKeyId;
    private String secretAccessKey;

    public boolean hasStaticCredentials() {
        if (accessKeyId == null || secretAccessKey == null) {
            return false;
        }
        if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            return false;
        }
        return true;
    }

    public boolean isBucketConfigured() {
        if (bucketName == null) {
            return false;
        }
        return !bucketName.isBlank();
    }
}

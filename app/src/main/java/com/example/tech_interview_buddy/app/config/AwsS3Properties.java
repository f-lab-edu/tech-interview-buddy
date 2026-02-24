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
        return accessKeyId != null && !accessKeyId.isBlank()
            && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    public boolean isBucketConfigured() {
        return bucketName != null && !bucketName.isBlank();
    }
}

package com.example.tech_interview_buddy.app.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    @NotBlank(message = "aws.s3.bucket-name must be configured")
    private String bucketName;
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    private int presignedUrlExpirationMinutes = 60;

    public boolean hasStaticCredentials() {
        if (accessKeyId == null || secretAccessKey == null) {
            return false;
        }
        if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            return false;
        }
        return true;
    }

}

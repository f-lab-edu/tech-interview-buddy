package com.example.tech_interview_buddy.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsS3Config {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        if (!properties.isBucketConfigured()) {
            throw new IllegalStateException("aws.s3.bucket-name must be configured");
        }
        S3ClientBuilder builder = S3Client.builder();
        configureCommon(builder, properties);
        return builder.build();
    }

    private void configureCommon(S3ClientBuilder builder, AwsS3Properties properties) {
        AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider(properties);
        builder.credentialsProvider(credentialsProvider);
        if (properties.getRegion() != null && !properties.getRegion().isBlank()) {
            builder.region(Region.of(properties.getRegion()));
        }
    }

    private AwsCredentialsProvider resolveCredentialsProvider(AwsS3Properties properties) {
        if (properties.hasStaticCredentials()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKeyId(),
                properties.getSecretAccessKey()
            );
            return StaticCredentialsProvider.create(credentials);
        }
        return DefaultCredentialsProvider.create();
    }
}

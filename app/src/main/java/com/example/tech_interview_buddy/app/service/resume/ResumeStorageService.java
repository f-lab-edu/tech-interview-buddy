package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.app.config.AwsS3Properties;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeStorageService {

    private static final String RESUME_PREFIX = "resumes";

    private final AwsS3Properties properties;
    private final S3Client s3Client;

    public String buildStorageKey(Long userId, Long resumeId, String originalFilename) {
        String safeFilename = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "resume.pdf";
        return RESUME_PREFIX + "/" + userId + "/" + resumeId + "/" + safeFilename;
    }

    public void uploadFile(MultipartFile file, String storageKey) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(storageKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            try (var inputStream = file.getInputStream()) {
                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
            }
            log.info("Uploaded resume to S3 - key: {}", storageKey);
        } catch (IOException | S3Exception e) {
            log.error("Failed to upload resume to S3 - key: {}", storageKey, e);
            throw new IllegalStateException("Failed to upload resume to storage", e);
        }
    }

    public void deleteFile(String storageKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(storageKey)
                .build();

            s3Client.deleteObject(request);
            log.info("Deleted resume from S3 - key: {}", storageKey);
        } catch (S3Exception e) {
            log.error("Failed to delete resume from S3 - key: {}", storageKey, e);
            throw new IllegalStateException("Failed to delete resume from storage", e);
        }
    }
}

package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.app.config.S3RequestFactory;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeStorageService {

    private static final String RESUME_PREFIX = "resumes";

    private final S3RequestFactory s3RequestFactory;
    private final S3Client s3Client;

    public String buildStorageKey(Long userId, Long resumeId, String originalFilename) {
        String safeFilename = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "resume.pdf";
        return RESUME_PREFIX + "/" + userId + "/" + resumeId + "/" + safeFilename;
    }

    public void uploadFile(MultipartFile file, String storageKey) {
        try (var inputStream = file.getInputStream()) {
            PutObjectRequest request = s3RequestFactory.putRequest(storageKey, file.getContentType(), file.getSize());
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("Uploaded resume to S3 - key: {}", storageKey);
        } catch (IOException | S3Exception e) {
            log.error("Failed to upload resume to S3 - key: {}", storageKey, e);
            throw new IllegalStateException("Failed to upload resume to storage", e);
        }
    }

    public InputStream downloadFile(String storageKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(storageKey)
                .build();
            return s3Client.getObject(request);
        } catch (S3Exception e) {
            log.error("Failed to download resume from S3 - key: {}", storageKey, e);
            throw new IllegalStateException("Failed to download resume from storage", e);
        }
    }

    public void deleteFile(String storageKey) {
        try {
            DeleteObjectRequest request = s3RequestFactory.deleteRequest(storageKey);
            s3Client.deleteObject(request);
            log.info("Deleted resume from S3 - key: {}", storageKey);
        } catch (S3Exception e) {
            log.error("Failed to delete resume from S3 - key: {}", storageKey, e);
            throw new IllegalStateException("Failed to delete resume from storage", e);
        }
    }
}

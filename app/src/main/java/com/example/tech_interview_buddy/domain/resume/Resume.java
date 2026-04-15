package com.example.tech_interview_buddy.domain.resume;

import com.example.tech_interview_buddy.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "resume")
@EntityListeners(AuditingEntityListener.class)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ResumeStatus status = ResumeStatus.UPLOADED;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "summarized_text", columnDefinition = "TEXT")
    private String summarizedText;

    @Column(name = "review_data", columnDefinition = "LONGTEXT")
    private String reviewData;

    @Column(name = "review_summary", columnDefinition = "TEXT")
    private String reviewSummary;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "analysis_started_at")
    private LocalDateTime analysisStartedAt;

    @Column(name = "analysis_completed_at")
    private LocalDateTime analysisCompletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Resume(User user, String originalFilename, Long fileSize, String mimeType, String storageKey) {
        this.user = user;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.storageKey = storageKey;
        this.status = ResumeStatus.UPLOADED;
    }

    public static Resume createUploaded(User user, String originalFilename, long fileSize, String mimeType, String storageKey) {
        return Resume.builder()
            .user(user)
            .originalFilename(originalFilename)
            .fileSize(fileSize)
            .mimeType(mimeType)
            .storageKey(storageKey)
            .build();
    }

    public void updateStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void markProcessing(LocalDateTime startedAt) {
        this.status = ResumeStatus.PROCESSING;
        this.analysisStartedAt = startedAt;
        this.failureReason = null;
    }

    public void markCompleted(LocalDateTime completedAt) {
        this.status = ResumeStatus.DONE;
        this.analysisCompletedAt = completedAt;
    }

    public void markFailed(String reason, LocalDateTime completedAt) {
        this.status = ResumeStatus.FAILED;
        this.failureReason = reason;
        this.analysisCompletedAt = completedAt;
    }

    public void saveExtractedText(String extractedText, String summarizedText) {
        this.extractedText = extractedText;
        this.summarizedText = summarizedText;
    }

    public void saveMarkdown(String markdown) {
        this.summarizedText = markdown;
    }

    public void saveReviewResult(String reviewData, String reviewSummary) {
        this.reviewData = reviewData;
        this.reviewSummary = reviewSummary;
    }
}

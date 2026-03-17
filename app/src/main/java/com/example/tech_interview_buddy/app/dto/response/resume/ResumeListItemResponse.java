package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResumeListItemResponse {

    private Long resumeId;
    private String filename;
    private String status;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private boolean hasReview;

    public static ResumeListItemResponse from(Resume resume) {
        return ResumeListItemResponse.builder()
            .resumeId(resume.getId())
            .filename(resume.getOriginalFilename())
            .status(resume.getStatus().name())
            .fileSize(resume.getFileSize())
            .uploadedAt(resume.getCreatedAt())
            .hasReview(resume.getReviewData() != null)
            .build();
    }
}

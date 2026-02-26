package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeUploadResponse {

    private Long resumeId;
    private String filename;
    private String status;

    public static ResumeUploadResponse from(Resume resume) {
        return ResumeUploadResponse.builder()
            .resumeId(resume.getId())
            .filename(resume.getOriginalFilename())
            .status(resume.getStatus().name())
            .build();
    }
}

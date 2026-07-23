package com.example.tech_interview_buddy.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RecruiterRequestCreateRequest(
    @NotBlank(message = "회사명은 필수입니다.")
    String companyName,
    String contactInfo
) {
}

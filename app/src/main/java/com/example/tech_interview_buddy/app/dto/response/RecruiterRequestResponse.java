package com.example.tech_interview_buddy.app.dto.response;

import com.example.tech_interview_buddy.domain.recruit.RecruiterRequest;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruiterRequestResponse {

    private Long id;
    private Long userId;
    private String username;
    private String companyName;
    private String contactInfo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static RecruiterRequestResponse from(RecruiterRequest request) {
        return RecruiterRequestResponse.builder()
            .id(request.getId())
            .userId(request.getUser().getId())
            .username(request.getUser().getUsername())
            .companyName(request.getCompanyName())
            .contactInfo(request.getContactInfo())
            .status(request.getStatus().name())
            .createdAt(request.getCreatedAt())
            .processedAt(request.getProcessedAt())
            .build();
    }
}

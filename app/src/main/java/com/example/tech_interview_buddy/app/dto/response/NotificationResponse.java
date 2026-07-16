package com.example.tech_interview_buddy.app.dto.response;

import com.example.tech_interview_buddy.domain.Notification;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long resumeId;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
            .notificationId(notification.getId())
            .message(notification.getMessage())
            .read(notification.isRead())
            .createdAt(notification.getCreatedAt())
            .resumeId(notification.getResumeId())
            .build();
    }
}

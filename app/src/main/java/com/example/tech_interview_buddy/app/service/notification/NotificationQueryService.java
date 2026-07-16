package com.example.tech_interview_buddy.app.service.notification;

import com.example.tech_interview_buddy.app.dto.response.NotificationResponse;
import com.example.tech_interview_buddy.domain.repository.NotificationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}

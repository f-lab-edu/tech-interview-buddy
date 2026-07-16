package com.example.tech_interview_buddy.app.service.notification;

import com.example.tech_interview_buddy.domain.Notification;
import com.example.tech_interview_buddy.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationReadService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "알림에 접근할 수 없습니다.");
        }

        notification.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}

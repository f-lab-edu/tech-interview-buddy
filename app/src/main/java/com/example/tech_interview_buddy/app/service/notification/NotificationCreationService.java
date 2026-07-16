package com.example.tech_interview_buddy.app.service.notification;

import com.example.tech_interview_buddy.domain.Notification;
import com.example.tech_interview_buddy.domain.repository.NotificationRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCreationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifySuccess(Resume resume) {
        String message = String.format("이력서 '%s' 분석이 완료되었습니다", resume.getOriginalFilename());
        notificationRepository.save(Notification.create(resume.getUser(), message, resume.getId()));
        log.info("Success notification created for resumeId={}", resume.getId());
    }

    @Transactional
    public void notifyFailure(Resume resume) {
        String message = String.format("이력서 '%s' 분석에 실패했습니다", resume.getOriginalFilename());
        notificationRepository.save(Notification.create(resume.getUser(), message, resume.getId()));
        log.info("Failure notification created for resumeId={}", resume.getId());
    }
}

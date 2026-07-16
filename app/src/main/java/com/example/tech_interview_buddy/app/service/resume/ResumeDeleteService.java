package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.NotificationRepository;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeQuestionRepository;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.resume.ResumeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeDeleteService {

    private final ResumeRepository resumeRepository;
    private final ResumeQuestionRepository questionRepository;
    private final NotificationRepository notificationRepository;
    private final ResumeStorageService storageService;

    @Transactional
    public void delete(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "이력서에 접근할 수 없습니다."));

        if (resume.getStatus() == ResumeStatus.PROCESSING) {
            log.warn("Deleting resume in PROCESSING state - resumeId={}, ongoing analysis will be interrupted", resumeId);
        }

        questionRepository.deleteByResumeId(resumeId);
        notificationRepository.deleteByResumeId(resumeId);
        storageService.deleteFile(resume.getStorageKey());
        resumeRepository.delete(resume);

        log.info("Resume deleted - id={}, userId={}", resumeId, userId);
    }
}

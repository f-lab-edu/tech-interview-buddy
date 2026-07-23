package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.exception.ResourceAccessDeniedException;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 이력서 소유권 검증을 담당하는 공통 컴포넌트.
 * 존재하지 않는 이력서와 남의 이력서를 구분하지 않고 접근 거부로 처리한다
 */
@Component
@RequiredArgsConstructor
public class ResumeAccessValidator {

    private final ResumeRepository resumeRepository;

    public Resume getOwnedResume(Long resumeId, Long userId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceAccessDeniedException("이력서에 접근할 수 없습니다."));
    }
}

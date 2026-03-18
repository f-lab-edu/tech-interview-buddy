package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeListItemResponse;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeQueryService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public Page<ResumeListItemResponse> listResumes(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(ResumeListItemResponse::from);
    }
}

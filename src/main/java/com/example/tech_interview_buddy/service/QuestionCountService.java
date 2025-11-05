package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionCountService {

    private final QuestionRepository questionRepository;

    /**
     * 검색 조건에 맞는 질문의 총 개수를 반환합니다.
     * 결과는 1시간 동안 캐싱되어 성능을 최적화합니다.
     *
     * @param searchRequest 검색 조건
     * @param currentUserId 현재 사용자 ID
     * @return 질문 총 개수
     */
    @Cacheable(
        value = "questionCount",
        key = "#searchRequest.category + ':' + #searchRequest.keyword + ':' + #searchRequest.tags + ':' + #searchRequest.isSolved + ':' + #currentUserId"
    )
    public long getTotalCount(QuestionSearchRequest searchRequest, Long currentUserId) {
        return questionRepository.countQuestions(searchRequest, currentUserId);
    }
}

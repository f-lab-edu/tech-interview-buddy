package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import com.example.tech_interview_buddy.domain.repository.QuestionRepository;
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
     * @param spec 검색 조건
     * @param currentUserId 현재 사용자 ID
     * @return 질문 총 개수
     */
    @Cacheable(
        value = "questionCount",
        key = "(#spec != null && #spec.category != null ? #spec.category.toString() : 'null') + ':' + (#spec != null && #spec.keyword != null ? #spec.keyword : 'null') + ':' + (#spec != null && #spec.tags != null ? #spec.tags.toString() : 'null') + ':' + (#spec != null && #spec.isSolved != null ? #spec.isSolved.toString() : 'null') + ':' + (#currentUserId != null ? #currentUserId.toString() : 'null')"
    )
    public long getTotalCount(QuestionSearchSpec spec, Long currentUserId) {
        return questionRepository.countQuestions(spec, currentUserId);
    }
}

package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface QuestionRepositoryCustom {

    /**
     * 동적 쿼리를 사용하여 복합 조건으로 질문을 검색합니다.
     *
     * @param searchRequest 검색 조건
     * @param pageable 페이징 정보
     * @param currentUserId 현재 사용자 ID (isSolved 필터링용)
     * @return 검색 결과
     */
    Page<Question> searchQuestions(QuestionSearchRequest searchRequest, Pageable pageable, Long currentUserId);
    
    /**
     * 동적 쿼리를 사용하여 검색 조건에 맞는 질문의 총 개수를 반환합니다.
     * 캐싱을 통해 성능을 최적화합니다.
     *
     * @param searchRequest 검색 조건
     * @param currentUserId 현재 사용자 ID (isSolved 필터링용)
     * @return 조건에 맞는 질문의 총 개수
     */
    long countQuestions(QuestionSearchRequest searchRequest, Long currentUserId);
    
}
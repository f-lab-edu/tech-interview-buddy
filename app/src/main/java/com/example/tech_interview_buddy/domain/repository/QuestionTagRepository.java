package com.example.tech_interview_buddy.domain.repository;

import com.example.tech_interview_buddy.domain.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    /**
     * 여러 Question의 QuestionTag를 Tag와 함께 배치 조회합니다.
     * JOIN FETCH로 Tag를 즉시 로딩하여 N+1 문제를 방지합니다.
     *
     * @param questionIds Question ID 리스트
     * @return QuestionTag 리스트 (Tag 포함)
     */
    @Query("SELECT qt FROM QuestionTag qt JOIN FETCH qt.tag WHERE qt.question.id IN :questionIds")
    List<QuestionTag> findByQuestionIdsWithTag(@Param("questionIds") List<Long> questionIds);
    
    /**
     * 태그 이름으로 Question ID를 조회합니다.
     * 태그 필터링을 위한 성능 최적화 메서드입니다.
     * EXISTS 서브쿼리 대신 이 메서드를 사용하여 먼저 ID를 필터링합니다.
     *
     * @param tagNames 태그 이름 리스트
     * @return 해당 태그를 가진 Question ID 리스트 (중복 제거)
     */
    @Query("SELECT DISTINCT qt.question.id FROM QuestionTag qt JOIN qt.tag t WHERE t.name IN :tagNames")
    List<Long> findQuestionIdsByTagNames(@Param("tagNames") List<String> tagNames);
}


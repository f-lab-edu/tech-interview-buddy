package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    Optional<Answer> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 사용자가 풀은 문제 ID들을 조회 (성능 최적화를 위해 ID만 조회)
     */
    @Query("SELECT a.question.id FROM Answer a WHERE a.user.id = :userId")
    Set<Long> findQuestionIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 질문들에 대해 사용자가 풀었는지 확인 (성능 최적화)
     * 전체 조회가 아닌 특정 질문들에 대해서만 조회
     */
    @Query("SELECT a.question.id FROM Answer a WHERE a.user.id = :userId AND a.question.id IN :questionIds")
    Set<Long> findQuestionIdsByUserIdAndQuestionIds(
        @Param("userId") Long userId, 
        @Param("questionIds") java.util.List<Long> questionIds
    );
    
} 
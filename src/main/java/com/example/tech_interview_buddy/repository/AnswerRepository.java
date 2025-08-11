package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    Optional<Answer> findByUserIdAndQuestionId(Long userId, Long questionId);
} 
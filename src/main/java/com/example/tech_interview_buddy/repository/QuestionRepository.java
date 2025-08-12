package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    Page<Question> findByCategory(Category category, Pageable pageable);
    
    Page<Question> findByIsSolved(Boolean isSolved, Pageable pageable);
    
    @Query("SELECT q FROM Question q WHERE q.content LIKE %:keyword%")
    Page<Question> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT q FROM Question q JOIN q.questionTags qt JOIN qt.tag t WHERE t.name IN :tagNames")
    Page<Question> findByTags(@Param("tagNames") List<String> tagNames, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Question q WHERE EXISTS (SELECT 1 FROM Answer a WHERE a.question = q AND a.user.id = :userId)")
    Page<Question> findSolvedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE NOT EXISTS (SELECT 1 FROM Answer a WHERE a.question = q AND a.user.id = :userId)")
    Page<Question> findUnsolvedByUserId(@Param("userId") Long userId, Pageable pageable);
} 
package com.example.tech_interview_buddy.dto.projection;

import com.example.tech_interview_buddy.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Question 목록 조회용 Projection DTO
 * FETCH JOIN 없이 필요한 데이터만 조회하여 성능 최적화
 */
@Getter
@AllArgsConstructor
public class QuestionListProjection {
    
    private Long id;
    private String content;
    private Category category;
    private LocalDateTime createdAt;
    private List<String> tagNames;
    private boolean isSolved;
    
    public QuestionListProjection(Long id, String content, Category category, LocalDateTime createdAt, boolean isSolved) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.tagNames = List.of(); // 기본값
        this.isSolved = isSolved;
    }
    
    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}

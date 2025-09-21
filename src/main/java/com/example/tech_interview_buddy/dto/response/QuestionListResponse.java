package com.example.tech_interview_buddy.dto.response;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuestionListResponse {
    
    private Long id;
    private String content;
    private Category category;
    private Boolean isSolved;
    private LocalDateTime createdAt;
    
    public static QuestionListResponse from(Question question, Boolean isSolvedByUser) {
        return QuestionListResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .isSolved(isSolvedByUser)
            .createdAt(question.getCreatedAt())
            .build();
    }
}
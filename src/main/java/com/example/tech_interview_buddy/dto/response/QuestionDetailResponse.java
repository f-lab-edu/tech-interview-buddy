package com.example.tech_interview_buddy.dto.response;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuestionDetailResponse {
    
    private Long id;
    private String content;
    private Category category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private MyAnswerResponse myAnswer;
    
    public static QuestionDetailResponse from(Question question) {
        return QuestionDetailResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .build();
    }
    
    @Getter
    @Builder
    public static class MyAnswerResponse {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
} 
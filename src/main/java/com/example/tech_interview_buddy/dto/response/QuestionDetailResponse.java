package com.example.tech_interview_buddy.dto.response;

import com.example.tech_interview_buddy.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QuestionDetailResponse {
    
    private Long id;
    private String content;
    private Category category;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private MyAnswerResponse myAnswer;
    
    @Getter
    @Builder
    public static class MyAnswerResponse {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
} 
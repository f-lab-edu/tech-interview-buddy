package com.example.tech_interview_buddy.app.dto.response;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QuestionListResponse {
    
    private Long id;
    private String content;
    private Category category;
    private Boolean isSolved;
    private LocalDateTime createdAt;
    private List<String> tags;  // 태그 목록 (배치 로딩)
    
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
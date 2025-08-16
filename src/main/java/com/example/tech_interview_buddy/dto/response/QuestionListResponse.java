package com.example.tech_interview_buddy.dto.response;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuestionListResponse {
    
    private Long id;
    private String content;
    private Category category;
    private List<String> tags;
    private Boolean isSolved;
    private LocalDateTime createdAt;
    
    public static QuestionListResponse from(Question question) {
        return QuestionListResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .tags(question.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList()))
            .isSolved(question.getIsSolved())
            .createdAt(question.getCreatedAt())
            .build();
    }
}
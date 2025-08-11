package com.example.tech_interview_buddy.dto.response;

import com.example.tech_interview_buddy.domain.Category;
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
    private List<String> tags;
    private Boolean isSolved;
    private LocalDateTime createdAt;
}
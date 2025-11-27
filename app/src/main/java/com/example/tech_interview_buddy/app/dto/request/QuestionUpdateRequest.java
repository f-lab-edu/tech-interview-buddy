package com.example.tech_interview_buddy.app.dto.request;

import com.example.tech_interview_buddy.common.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionUpdateRequest {
    private String content;
    private Category category;
    private List<String> tags;
    
    @Builder
    public QuestionUpdateRequest(String content, Category category, List<String> tags) {
        this.content = content;
        this.category = category;
        this.tags = tags;
    }
}

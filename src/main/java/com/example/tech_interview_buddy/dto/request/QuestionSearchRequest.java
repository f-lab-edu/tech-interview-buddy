package com.example.tech_interview_buddy.dto.request;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.dto.enums.SortDirection;
import com.example.tech_interview_buddy.dto.enums.SortField;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionSearchRequest {
    
    private Category category;
    private String keyword;
    private List<String> tags;
    private Boolean isSolved;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private SortField sort = SortField.ID;
    
    @Builder.Default
    private SortDirection direction = SortDirection.ASC;
} 
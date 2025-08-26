package com.example.tech_interview_buddy.dto.request;

import com.example.tech_interview_buddy.domain.Category;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchRequest {
    
    private Category category;
    private String keyword;
    private List<String> tags;
    private Boolean isSolved;
    private int page = 0;
    private int size = 20;
    private String sort = "id";
    private String direction = "asc";
} 
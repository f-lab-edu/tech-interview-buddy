package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.common.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Question 검색 조건을 담는 도메인 객체
 */
@Getter
@Builder
public class QuestionSearchCriteria {
    private Category category;
    private String keyword;
    private List<String> tags;
    private Boolean isSolved;
    private int page;
    private int size;
    private String sortField;
    private String sortDirection;
}


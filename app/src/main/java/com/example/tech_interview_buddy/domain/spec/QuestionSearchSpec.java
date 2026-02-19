package com.example.tech_interview_buddy.domain.spec;

import com.example.tech_interview_buddy.common.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Domain 모듈 전용 검색 조건 스펙
 * API DTO와 독립적인 도메인 계층 객체
 */
@Getter
@Builder
public class QuestionSearchSpec {
    private Category category;
    private String keyword;
    private List<String> tags;
    private Boolean isSolved;
    private int page;
    private int size;
    private String sortField;
    private String sortDirection;
}


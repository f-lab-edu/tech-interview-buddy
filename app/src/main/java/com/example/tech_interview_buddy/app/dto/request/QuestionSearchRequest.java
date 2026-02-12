package com.example.tech_interview_buddy.app.dto.request;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.app.dto.enums.SortDirection;
import com.example.tech_interview_buddy.app.dto.enums.SortField;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionSearchRequest {

    private final Category category;
    private final String keyword;
    private final List<String> tags;
    private final Boolean isSolved;
    private final int page;
    private final int size;
    private final SortField sort;
    private final SortDirection direction;

    @Builder
    public QuestionSearchRequest(
            Category category, String keyword, List<String> tags, Boolean isSolved,
            Integer page, Integer size, SortField sort, SortDirection direction) {
        this.category = category;
        this.keyword = keyword;
        this.tags = tags;
        this.isSolved = isSolved;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
        this.sort = sort != null ? sort : SortField.ID;
        this.direction = direction != null ? direction : SortDirection.ASC;
    }
}

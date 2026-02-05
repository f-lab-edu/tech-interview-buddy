package com.example.tech_interview_buddy.app.dto.request;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.app.dto.enums.SortDirection;
import com.example.tech_interview_buddy.app.dto.enums.SortField;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class QuestionSearchRequest {

    private Category category;
    private String keyword;
    private java.util.List<String> tags;
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

package com.example.tech_interview_buddy.dto.request;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.dto.enums.SortDirection;
import com.example.tech_interview_buddy.dto.enums.SortField;
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

// 1. 테스트 코드 (QuestionRepository, utils쪽 builder 관련)
// TDD
// JUnit, Jupiter >>> Spock 선호
// 리파지토리 테스트는 통합 테스트이다.
// 각 테스트 별 격리가 필요. DB 작업이 필요 (DB 유닛: XML로 테이블의 데이터를 관리할 수 있다. 각 테스트 메서드마다 DB
// 테스트 용 DB는 별도로 분리해야 한다.
// 2. 요청 분석
// 그라파나, 로그백
//
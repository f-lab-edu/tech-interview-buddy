package com.example.tech_interview_buddy.app.dto.response;

import com.example.tech_interview_buddy.app.dto.external.RecommendedQuestion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionSearchResponse {
    
    private List<QuestionListResponse> contents;  // 문제 객체들의 배열
    private List<RecommendedQuestion> recommendations;  // 추천 질문 목록
}



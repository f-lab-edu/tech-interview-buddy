package com.example.tech_interview_buddy.app.service;

import com.example.tech_interview_buddy.app.dto.external.RecommendedQuestion;
import com.example.tech_interview_buddy.domain.service.QuestionSearchResult;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class QuestionSearchWithRecommendResult {
    private Page<QuestionSearchResult> searchResults;
    private List<RecommendedQuestion> recommendedQuestions;
}

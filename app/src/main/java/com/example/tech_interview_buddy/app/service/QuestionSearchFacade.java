package com.example.tech_interview_buddy.app.service;

import com.example.tech_interview_buddy.app.client.RecommendServiceClient;
import com.example.tech_interview_buddy.app.dto.external.RecommendRequest;
import com.example.tech_interview_buddy.app.dto.external.RecommendResponse;
import com.example.tech_interview_buddy.app.dto.external.RecommendedQuestion;
import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.domain.service.QuestionSearchResult;
import com.example.tech_interview_buddy.domain.service.QuestionService;
import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 질문 검색(도메인)과 추천 서비스(외부) 결과를 조합한다.
 * 도메인 서비스가 외부 연동을 알지 않도록 오케스트레이션을 이 계층에서 담당한다.
 */
@Service
@RequiredArgsConstructor
public class QuestionSearchFacade {

    private final QuestionService questionService;
    private final RecommendServiceClient recommendServiceClient;

    public QuestionSearchWithRecommendResult searchQuestionsWithRecommend(
            QuestionSearchSpec spec,
            Long currentUserId,
            Category category,
            List<String> tags) {

        Page<QuestionSearchResult> searchResults = questionService.searchQuestions(spec, currentUserId);

        RecommendResponse recommendResponse = getRecommendResponse(category, tags);

        List<RecommendedQuestion> recommendedQuestions =
                Optional.ofNullable(recommendResponse)
                        .map(RecommendResponse::getRecommendedQuestions)
                        .orElse(Collections.emptyList());

        return QuestionSearchWithRecommendResult.builder()
                .searchResults(searchResults)
                .recommendedQuestions(recommendedQuestions)
                .build();
    }

    public RecommendResponse getRecommendResponse(Category category, List<String> tags) {
        RecommendRequest recommendRequest = RecommendRequest.builder()
                .category(category != null ? category.toString() : null)
                .tags(tags)
                .build();

        return recommendServiceClient.callRecommendService(recommendRequest);
    }
}

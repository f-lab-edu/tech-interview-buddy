package com.example.tech_interview_buddy.app.mapper;

import com.example.tech_interview_buddy.app.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.domain.service.QuestionSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Question 도메인 객체를 DTO로 변환하는 Mapper
 */
@Component
public class QuestionMapper {

    public QuestionListResponse toQuestionListResponse(QuestionSearchResult result) {
        return QuestionListResponse.builder()
            .id(result.getQuestion().getId())
            .content(result.getQuestion().getContent())
            .category(result.getQuestion().getCategory())
            .isSolved(result.isSolved())
            .createdAt(result.getQuestion().getCreatedAt())
            .tags(result.getTags())
            .build();
    }

    public List<QuestionListResponse> toQuestionListResponseList(List<QuestionSearchResult> results) {
        return results.stream()
            .map(this::toQuestionListResponse)
            .collect(Collectors.toList());
    }
}

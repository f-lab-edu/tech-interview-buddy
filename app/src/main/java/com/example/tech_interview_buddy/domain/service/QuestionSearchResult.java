package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Question 검색 결과를 담는 도메인 객체
 * Question과 함께 추가 정보(태그, solved 상태)를 포함
 */
@Getter
@Builder
public class QuestionSearchResult {
    private Question question;
    private boolean isSolved;
    private List<String> tags;
}


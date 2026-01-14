package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Builder;
import lombok.Getter;

/**
 * Question과 함께 Answer 정보를 담는 도메인 객체
 */
@Getter
@Builder
public class QuestionWithAnswer {
    private Question question;
    private Answer answer;
}


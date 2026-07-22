package com.example.tech_interview_buddy.app.dto.external;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendResponse {
	private List<RecommendedQuestion> recommendedQuestions;
}


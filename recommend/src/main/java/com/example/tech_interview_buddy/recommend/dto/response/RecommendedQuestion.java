package com.example.tech_interview_buddy.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedQuestion {
	private Long id;
	private String content;
	private String category;
}

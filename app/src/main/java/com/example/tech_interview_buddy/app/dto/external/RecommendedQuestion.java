package com.example.tech_interview_buddy.app.dto.external;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedQuestion {
	private Long id;
	private String content;
	private String category;
}

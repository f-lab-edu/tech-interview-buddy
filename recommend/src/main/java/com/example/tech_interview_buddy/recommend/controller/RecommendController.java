package com.example.tech_interview_buddy.recommend.controller;

import com.example.tech_interview_buddy.recommend.dto.request.RecommendRequest;
import com.example.tech_interview_buddy.recommend.dto.response.RecommendResponse;
import com.example.tech_interview_buddy.recommend.dto.response.RecommendedQuestion;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RecommendController {

	@PostMapping("/recommend")
	public RecommendResponse recommend(@RequestBody RecommendRequest request) {
		// 더미 데이터: 추천 관련 질문들
		List<RecommendedQuestion> recommendedQuestions = Arrays.asList(
			RecommendedQuestion.builder()
				.id(1L)
				.content("Spring Boot에서 외부 API를 호출하는 방법은?")
				.category(request.getCategory() != null ? request.getCategory() : "PROGRAMMING")
				.build(),
			RecommendedQuestion.builder()
				.id(2L)
				.content("RESTful API 설계 원칙에 대해 설명해주세요.")
				.category(request.getCategory() != null ? request.getCategory() : "PROGRAMMING")
				.build(),
			RecommendedQuestion.builder()
				.id(3L)
				.content("마이크로서비스 아키텍처의 장단점은?")
				.category(request.getCategory() != null ? request.getCategory() : "SYSTEM_DESIGN")
				.build()
		);
		
		return RecommendResponse.builder()
			.recommendedQuestions(recommendedQuestions)
			.build();
	}
}


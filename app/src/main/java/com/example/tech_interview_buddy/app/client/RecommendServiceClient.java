package com.example.tech_interview_buddy.app.client;

import com.example.tech_interview_buddy.app.dto.external.RecommendRequest;
import com.example.tech_interview_buddy.app.dto.external.RecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;


@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendServiceClient {

	private final RestTemplate restTemplate;

	@Value("${external.service.url:http://localhost:8081}")
	private String recommendServiceUrl;

	public RecommendResponse callRecommendService(RecommendRequest request) {
		try {
			String url = recommendServiceUrl + "/api/v1/recommend";
			return restTemplate.postForObject(url, request, RecommendResponse.class);
		} catch (ResourceAccessException e) {
			log.warn("Recommend 서비스 호출 실패 (타임아웃 또는 연결 오류): {}", e.getMessage());
			return RecommendResponse.builder()
				.recommendedQuestions(Collections.emptyList())
				.build();
		} catch (RestClientException e) {
			log.warn("Recommend 서비스 호출 실패: {}", e.getMessage());
			return RecommendResponse.builder()
				.recommendedQuestions(Collections.emptyList())
				.build();
		} catch (Exception e) {
			log.error("Recommend 서비스 호출 중 예상치 못한 오류 발생", e);
			return RecommendResponse.builder()
				.recommendedQuestions(Collections.emptyList())
				.build();
		}
	}
}


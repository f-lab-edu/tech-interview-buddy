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
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;


@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendServiceClient {

	private final RestTemplate restTemplate;

	@Value("${external.service.url:http://localhost:8081}")
	private String recommendServiceUrl;

	public RecommendResponse callRecommendService(RecommendRequest request) {
		String url = recommendServiceUrl + "/api/v1/recommend";
		
		try {
			log.debug("Recommend 서비스 호출 시작 - URL: {}, 요청: {}", url, request);
			RecommendResponse response = restTemplate.postForObject(url, request, RecommendResponse.class);
			
			if (response == null) {
				log.warn("Recommend 서비스가 null 응답을 반환했습니다.");
				return createEmptyResponse();
			}
			
			log.debug("Recommend 서비스 호출 성공 - 추천 질문 {}개",
				response.getRecommendedQuestions() != null ? response.getRecommendedQuestions().size() : 0);
			return response;
		} catch (RestClientException e) {
			logRestClientException(e, url);
			return createEmptyResponse();
		} catch (Exception e) {
			log.error("Recommend 서비스 호출 중 예상치 못한 오류 발생 - URL: {}", url, e);
			return createEmptyResponse();
		}
	}
	
	private RecommendResponse createEmptyResponse() {
		return RecommendResponse.builder()
			.recommendedQuestions(Collections.emptyList())
			.build();
	}

	private void logRestClientException(RestClientException e, String url) {
		if (e instanceof ResourceAccessException) {
			log.warn("Recommend 서비스 연결 실패 (타임아웃 또는 연결 오류) - URL: {}, 오류: {}",
				url, e.getMessage());
			return;
		}
		if (e instanceof RestClientResponseException responseException) {
			if (responseException.getStatusCode().value() >= 500) {
				log.error("Recommend 서비스 서버 오류 - URL: {}, 상태 코드: {}, 응답: {}",
					url, responseException.getStatusCode(), responseException.getResponseBodyAsString());
			} else {
				log.warn("Recommend 서비스 클라이언트 오류 - URL: {}, 상태 코드: {}, 응답: {}",
					url, responseException.getStatusCode(), responseException.getResponseBodyAsString());
			}
			return;
		}
		log.warn("Recommend 서비스 호출 실패 - URL: {}, 오류: {}", url, e.getMessage());
	}
}


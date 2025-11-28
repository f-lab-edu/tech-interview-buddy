package com.example.tech_interview_buddy.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3000); // 연결 타임아웃 3초
		factory.setReadTimeout(3000); // 읽기 타임아웃 3초
		
		return new RestTemplate(factory);
	}
}


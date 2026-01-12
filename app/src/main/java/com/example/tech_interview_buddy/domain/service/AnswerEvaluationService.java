package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.app.config.OpenAiConfig;
import com.example.tech_interview_buddy.domain.Question;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerEvaluationService {

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;
    private final AnswerEvaluationPromptTemplate promptTemplate;

    private static final String MODEL = "gpt-4o-mini";
    private static final double TEMPERATURE = 0.3;
    private static final int MAX_TOKENS = 1000;

    public String evaluateAnswer(Question question, String answerContent) {
        if (!openAiConfig.isConfigured()) {
            log.warn("OpenAI API key is not configured. Skipping evaluation.");
            return null;
        }

        try {
            String prompt = promptTemplate.buildPrompt(
                    question.getContent(),
                    answerContent,
                    question.getCategory()
            );

            String url = openAiConfig.getApiUrl() + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = openAiConfig.getApiKey();
            if (apiKey != null) {
                headers.setBearerAuth(apiKey);
            }

            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", TEMPERATURE,
                    "max_tokens", MAX_TOKENS
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.debug("Calling OpenAI API: {}", url);
            log.debug("Request body: {}", requestBody);

            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ChatCompletionResponse.class
            );

            log.debug("OpenAI API response status: {}", response.getStatusCode());
            log.debug("OpenAI API response body: {}", response.getBody());

            ChatCompletionResponse body = response.getBody();
            if (body != null &&
                    body.getChoices() != null &&
                    !body.getChoices().isEmpty()) {

                String feedback = body.getChoices().get(0).getMessage().getContent();
                log.debug("Answer evaluation completed successfully for question ID: {}", question.getId());
                return feedback;
            }

            log.warn("Empty response from OpenAI API for question ID: {}", question.getId());
            return null;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("OpenAI API quota exceeded (429 Too Many Requests) for question ID: {}. " +
                        "Please check your OpenAI plan and billing details.", question.getId());
            } else if (e.getStatusCode().value() == 401) {
                log.error("OpenAI API authentication failed (401 Unauthorized) for question ID: {}. " +
                        "Please check your API key.", question.getId());
            } else {
                log.error("OpenAI API HTTP error ({}): {} for question ID: {}",
                        e.getStatusCode().value(), e.getMessage(), question.getId());
            }
            log.error("Response body: {}", e.getResponseBodyAsString());
            return null;
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("RestClientException while evaluating answer for question ID: {}", question.getId(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while evaluating answer for question ID: {}", question.getId(), e);
            log.error("Error class: {}, message: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            return null;
        }
    }

    @Data
    private static class ChatCompletionResponse {
        private List<Choice> choices;

        @Data
        private static class Choice {
            private Message message;

            @Data
            private static class Message {
                private String content;
            }
        }
    }
}


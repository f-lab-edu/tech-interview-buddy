package com.example.tech_interview_buddy.domain.service.ai;

import com.example.tech_interview_buddy.app.config.OpenAiConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIAdapter implements AiAdapter {

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    private static final String MODEL = "gpt-4o-mini";
    private static final double TEMPERATURE = 0.3;
    private static final int MAX_TOKENS = 1000;

    @Override
    public String sendPrompt(String prompt) {
        if (!openAiConfig.isConfigured()) {
            log.warn("OpenAI API key is not configured. Skipping API call.");
            return null;
        }

        try {
            ResponseEntity<ChatCompletionResponse> response = callOpenAiApi(prompt);
            return extractContent(response);
        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
            return null;
        } catch (RestClientException e) {
            log.error("RestClientException while calling OpenAI API", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while calling OpenAI API", e);
            return null;
        }
    }

    private ResponseEntity<ChatCompletionResponse> callOpenAiApi(String prompt) {
        String url = openAiConfig.getApiUrl();
        HttpHeaders headers = createHeaders();
        Map<String, Object> requestBody = createRequestBody(prompt);
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

        return response;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = openAiConfig.getApiKey();
        if (apiKey != null) {
            headers.setBearerAuth(apiKey);
        }
        return headers;
    }

    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", TEMPERATURE,
                "max_tokens", MAX_TOKENS
        );
    }

    private String extractContent(ResponseEntity<ChatCompletionResponse> response) {
        ChatCompletionResponse body = response.getBody();
        if (body != null &&
                body.getChoices() != null &&
                !body.getChoices().isEmpty()) {
            return body.getChoices().get(0).getMessage().getContent();
        }
        log.warn("Empty response from OpenAI API");
        return null;
    }

    private void handleHttpClientError(HttpClientErrorException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());

        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            log.error("OpenAI API quota exceeded (429 Too Many Requests). " +
                    "Please check your OpenAI plan and billing details.");
        } else if (status == HttpStatus.UNAUTHORIZED) {
            log.error("OpenAI API authentication failed (401 Unauthorized). " +
                    "Please check your API key.");
        } else {
            log.error("OpenAI API HTTP error ({}): {}",
                    status.value(), e.getMessage());
        }
        log.error("Response body: {}", e.getResponseBodyAsString());
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
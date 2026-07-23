package com.example.tech_interview_buddy.domain.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 호출 + JSON 추출 + 파싱 + 1회 재시도를 담당하는 공통 클라이언트.
 * 각 AI 서비스는 프롬프트 생성과 결과 저장만 책임진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiJsonClient {

    private final AiAdapter aiAdapter;
    private final ObjectMapper objectMapper;

    /**
     * JSON 객체({...}) 응답을 기대할 때 사용한다.
     *
     * @throws RuntimeException 재시도 후에도 실패하면 onFailure가 만든 예외를 던진다
     */
    public <T> T requestObject(String prompt, Class<T> type, Supplier<RuntimeException> onFailure) {
        return callWithRetry(prompt, raw -> parse(extractBlock(raw, '{', '}'), type), onFailure);
    }

    /**
     * JSON 배열([...]) 응답을 기대할 때 사용한다.
     *
     * @throws RuntimeException 재시도 후에도 실패하면 onFailure가 만든 예외를 던진다
     */
    public <T> List<T> requestList(String prompt, TypeReference<List<T>> type, Supplier<RuntimeException> onFailure) {
        return callWithRetry(prompt, raw -> parse(extractBlock(raw, '[', ']'), type), onFailure);
    }

    private <R> R callWithRetry(String prompt, Function<String, R> parser, Supplier<RuntimeException> onFailure) {
        R result = tryOnce(prompt, parser);
        if (result != null) {
            return result;
        }

        log.warn("AI response parse failed on first attempt, retrying...");
        result = tryOnce(prompt, parser);
        if (result != null) {
            return result;
        }

        throw onFailure.get();
    }

    private <R> R tryOnce(String prompt, Function<String, R> parser) {
        String raw = aiAdapter.sendPrompt(prompt);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return parser.apply(raw);
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * AI 응답에서 가장 바깥 JSON 블록만 잘라낸다 (앞뒤 설명 문구, 마크다운 펜스 제거).
     * 블록을 찾지 못하면 원문을 그대로 반환해 파싱 실패 → 재시도 경로로 수렴시킨다.
     */
    private String extractBlock(String raw, char open, char close) {
        int start = raw.indexOf(open);
        int end = raw.lastIndexOf(close);
        if (start == -1 || end == -1 || start >= end) {
            return raw;
        }
        return raw.substring(start, end + 1);
    }

    private <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T parse(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

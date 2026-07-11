package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeReviewResponse;
import com.example.tech_interview_buddy.domain.service.ai.AiJsonClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiReviewService {

    private final AiJsonClient aiJsonClient;
    private final ResumeReviewPromptTemplate promptTemplate;
    private final ObjectMapper objectMapper;

    /**
     * AI 리뷰를 생성해 반환한다. 저장은 호출자가 담당한다.
     * 파싱 실패 시 1회 재시도한다.
     *
     * @throws ResumeAiReviewException AI 호출 또는 파싱이 모두 실패한 경우
     */
    public Result generate(String extractedText) {
        String prompt = promptTemplate.buildPrompt(extractedText);

        ResumeReviewResponse review = aiJsonClient.requestObject(
                prompt,
                ResumeReviewResponse.class,
                () -> new ResumeAiReviewException("AI 리뷰 응답을 파싱하는 데 실패했습니다."));

        log.info("AI review generated");
        return new Result(serialize(review), review.getSummary());
    }

    /** 저장에 필요한 리뷰 결과(직렬화된 JSON과 요약). */
    public record Result(String reviewJson, String summary) {}

    private String serialize(ResumeReviewResponse review) {
        try {
            return objectMapper.writeValueAsString(review);
        } catch (Exception e) {
            throw new ResumeAiReviewException("AI 리뷰 결과를 직렬화하는 데 실패했습니다.", e);
        }
    }
}

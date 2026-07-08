package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeReviewResponse;
import com.example.tech_interview_buddy.domain.resume.Resume;
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
     * AI 리뷰를 생성하고 Resume 엔티티에 반영한다.
     * 파싱 실패 시 1회 재시도한다.
     *
     * @throws ResumeAiReviewException AI 호출 또는 파싱이 모두 실패한 경우
     */
    public void generate(Resume resume) {
        String prompt = promptTemplate.buildPrompt(resume.getExtractedText());

        ResumeReviewResponse review = aiJsonClient.requestObject(
                prompt,
                ResumeReviewResponse.class,
                () -> new ResumeAiReviewException("AI 리뷰 응답을 파싱하는 데 실패했습니다."));

        String reviewJson = serialize(review);
        resume.saveReviewResult(reviewJson, review.getSummary());

        log.info("AI review generated for resumeId={}", resume.getId());
    }

    private String serialize(ResumeReviewResponse review) {
        try {
            return objectMapper.writeValueAsString(review);
        } catch (Exception e) {
            throw new ResumeAiReviewException("AI 리뷰 결과를 직렬화하는 데 실패했습니다.", e);
        }
    }
}

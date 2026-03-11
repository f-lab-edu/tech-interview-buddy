package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeReviewResponse;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiReviewService {

    private final AiAdapter aiAdapter;
    private final ResumeReviewPromptTemplate promptTemplate;
    private final ObjectMapper objectMapper;
    private final ResumeRepository resumeRepository;

    /**
     * AI 리뷰를 생성하고 Resume 엔티티에 저장한다.
     * 파싱 실패 시 1회 재시도한다.
     *
     * @throws ResumeAiReviewException AI 호출 또는 파싱이 모두 실패한 경우
     */
    @Transactional
    public void generateAndSave(Resume resume) {
        String text = resume.getExtractedText();

        ResumeReviewResponse review = callWithRetry(text);

        String reviewJson = serialize(review);
        resume.saveReviewResult(reviewJson, review.getSummary());
        resumeRepository.save(resume);

        log.info("AI review saved for resumeId={}", resume.getId());
    }

    private ResumeReviewResponse callWithRetry(String resumeText) {
        String prompt = promptTemplate.buildPrompt(resumeText);

        String rawResponse = aiAdapter.sendPrompt(prompt);
        ResumeReviewResponse review = tryParse(rawResponse);

        if (review != null) {
            return review;
        }

        log.warn("AI review parse failed on first attempt, retrying...");
        rawResponse = aiAdapter.sendPrompt(prompt);
        review = tryParse(rawResponse);

        if (review != null) {
            return review;
        }

        throw new ResumeAiReviewException("AI 리뷰 응답을 파싱하는 데 실패했습니다.");
    }

    private ResumeReviewResponse tryParse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }
        try {
            String json = extractJson(rawResponse);
            return objectMapper.readValue(json, ResumeReviewResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse AI review response: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || start >= end) {
            return raw;
        }
        return raw.substring(start, end + 1);
    }

    private String serialize(ResumeReviewResponse review) {
        try {
            return objectMapper.writeValueAsString(review);
        } catch (Exception e) {
            throw new ResumeAiReviewException("AI 리뷰 결과를 직렬화하는 데 실패했습니다.", e);
        }
    }
}

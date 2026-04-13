package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeScoreRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import com.example.tech_interview_buddy.domain.resume.ScoringCriteria;
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiScoringService {

    private final AiAdapter aiAdapter;
    private final ResumeScoringPromptTemplate promptTemplate;
    private final ObjectMapper objectMapper;
    private final ResumeScoreRepository scoreRepository;

    @Transactional
    public void generateAndSave(Resume resume) {
        String text = resume.getExtractedText();

        List<ScoreItem> scores = callWithRetry(text);
        if (scores == null || scores.isEmpty()) {
            log.warn("AI returned no scores for resumeId={}", resume.getId());
            return;
        }

        List<ResumeScore> entities = scores.stream()
                .map(item -> ResumeScore.builder()
                        .resume(resume)
                        .criteria(parseCriteria(item.getCriteria()))
                        .score(clampScore(item.getScore()))
                        .maxScore(10)
                        .comment(item.getComment())
                        .build())
                .collect(Collectors.toList());

        scoreRepository.saveAll(entities);
        log.info("Saved {} scores for resumeId={}", entities.size(), resume.getId());
    }

    private List<ScoreItem> callWithRetry(String resumeText) {
        String prompt = promptTemplate.buildPrompt(resumeText);

        String rawResponse = aiAdapter.sendPrompt(prompt);
        List<ScoreItem> scores = tryParse(rawResponse);

        if (scores != null) {
            return scores;
        }

        log.warn("AI scoring parse failed on first attempt, retrying...");
        rawResponse = aiAdapter.sendPrompt(prompt);
        scores = tryParse(rawResponse);

        if (scores != null) {
            return scores;
        }

        throw new ResumeAiScoringException("AI 점수 평가 응답을 파싱하는 데 실패했습니다.");
    }

    private List<ScoreItem> tryParse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }
        try {
            String json = extractJsonArray(rawResponse);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse AI scoring response: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || start >= end) {
            return raw;
        }
        return raw.substring(start, end + 1);
    }

    private ScoringCriteria parseCriteria(String value) {
        try {
            return ScoringCriteria.valueOf(value);
        } catch (Exception e) {
            log.warn("Unknown scoring criteria '{}', defaulting to OVERALL_STRUCTURE", value);
            return ScoringCriteria.OVERALL_STRUCTURE;
        }
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(10, score));
    }

    @Getter
    @Builder
    @Jacksonized
    static class ScoreItem {
        private String criteria;
        private int score;
        private int maxScore;
        private String comment;
    }
}

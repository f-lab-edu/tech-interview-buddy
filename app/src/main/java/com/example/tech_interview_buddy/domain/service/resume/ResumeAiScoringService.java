package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.domain.exception.ResumeAiScoringException;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeScoreRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import com.example.tech_interview_buddy.domain.resume.ScoringCriteria;
import com.example.tech_interview_buddy.domain.service.ai.AiJsonClient;
import com.fasterxml.jackson.core.type.TypeReference;
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

    private final AiJsonClient aiJsonClient;
    private final ResumeScoringPromptTemplate promptTemplate;
    private final ResumeScoreRepository scoreRepository;

    @Transactional
    public void generateAndSave(Resume resume) {
        String prompt = promptTemplate.buildPrompt(resume.getExtractedText());

        List<ScoreItem> scores = aiJsonClient.requestList(
                prompt,
                new TypeReference<List<ScoreItem>>() {},
                () -> new ResumeAiScoringException("AI 점수 평가 응답을 파싱하는 데 실패했습니다."));

        if (scores.isEmpty()) {
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

package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.common.domain.Difficulty;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeQuestionRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.resume.ResumeQuestion;
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
public class ResumeAiQuestionService {

    private final AiJsonClient aiJsonClient;
    private final ResumeQuestionPromptTemplate promptTemplate;
    private final ResumeQuestionRepository questionRepository;

    /**
     * AI 예상질문을 생성하고 DB에 저장한다.
     * 실패해도 이미 저장된 리뷰는 유지하고 예외를 던진다 (오케스트레이터가 처리).
     */
    @Transactional
    public void generateAndSave(Resume resume) {
        String prompt = promptTemplate.buildPrompt(resume.getExtractedText());

        List<QuestionItem> questions = aiJsonClient.requestList(
                prompt,
                new TypeReference<List<QuestionItem>>() {},
                () -> new ResumeAiQuestionException("AI 예상질문 응답을 파싱하는 데 실패했습니다."));

        if (questions.isEmpty()) {
            log.warn("AI returned no questions for resumeId={}", resume.getId());
            return;
        }

        List<ResumeQuestion> entities = questions.stream()
                .map((QuestionItem q) -> ResumeQuestion.builder()
                        .resume(resume)
                        .questionText(q.getQuestionText())
                        .category(parseCategory(q.getCategory()))
                        .difficulty(parseDifficulty(q.getDifficulty()))
                        .sourceSection(q.getSourceSection())
                        .sourceQuote(q.getSourceQuote())
                        .build())
                .collect(Collectors.toList());

        questionRepository.saveAll(entities);
        log.info("Saved {} questions for resumeId={}", entities.size(), resume.getId());
    }

    private Category parseCategory(String value) {
        try {
            return Category.valueOf(value);
        } catch (Exception e) {
            log.warn("Unknown category '{}', defaulting to PROGRAMMING", value);
            return Category.PROGRAMMING;
        }
    }

    private Difficulty parseDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (Exception e) {
            log.warn("Unknown difficulty '{}', defaulting to MEDIUM", value);
            return Difficulty.MEDIUM;
        }
    }

    @Getter
    @Builder
    @Jacksonized
    private static class QuestionItem {
        private String questionText;
        private String category;
        private String difficulty;
        private String sourceSection;
        private String sourceQuote;
    }
}

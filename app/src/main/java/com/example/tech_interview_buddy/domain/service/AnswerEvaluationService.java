package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.repository.AnswerRepository;
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerEvaluationService {


    private final AiAdapter aiAdapter;
    private final AnswerEvaluationPromptTemplate promptTemplate;
    private final AnswerRepository answerRepository;

    public String evaluateAnswer(Question question, String answerContent) {
        String prompt = promptTemplate.buildPrompt(
                question.getContent(),
                answerContent,
                question.getCategory()
        );

        String feedback = aiAdapter.sendPrompt(prompt);

        if (feedback != null) {
            log.debug("Answer evaluation completed successfully for question ID: {}", question.getId());
        } else {
            log.warn("Answer evaluation returned null for question ID: {}", question.getId());
        }

        return feedback;
    }

    @Async
    @Transactional
    public void evaluateAnswerAsync(Long answerId, Question question, String answerContent) {
        log.info("Starting async evaluation for answer ID: {}", answerId);

        String feedback = evaluateAnswer(question, answerContent);

        if (feedback != null) {
            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found with id: " + answerId));

            answer.updateEvaluation(feedback);
            answerRepository.save(answer);

            log.info("Evaluation completed and saved for answer ID: {}", answerId);
        } else {
            log.warn("Evaluation returned null for answer ID: {}", answerId);
        }
    }
}
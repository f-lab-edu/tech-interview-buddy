package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.repository.AnswerRepository;
import com.example.tech_interview_buddy.domain.repository.QuestionRepository;
import com.example.tech_interview_buddy.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Domain 서비스: Answer 도메인 로직
 * Security 의존성 제거, userId를 파라미터로 받음
 */
@Service
@Transactional(readOnly = true)
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerEvaluationService answerEvaluationService;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository,
                         UserRepository userRepository, AnswerEvaluationService answerEvaluationService) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.answerEvaluationService = answerEvaluationService;
    }

    @Transactional
    public Answer createAnswer(Long questionId, Long userId, String content) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(content)
                .build();

        answer = answerRepository.save(answer);
        answerRepository.flush();
        answerEvaluationService.evaluateAnswerAsync(answer.getId(), question, content);

        return answer;
    }

    @Transactional
    public Answer updateAnswer(Long answerId, Long userId, String content) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (!answer.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own answer");
        }

        answer.updateContent(content);
        return answer;
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (!answer.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own answer");
        }

        answerRepository.delete(answer);
    }

    public Optional<Answer> getMyAnswer(Long questionId, Long userId) {
        return answerRepository.findTopByUserIdAndQuestionIdOrderByCreatedAtDesc(userId, questionId);
    }

    public Set<Long> getSolvedQuestionIdsByUserAndQuestions(Long userId, java.util.List<Long> questionIds) {
        return answerRepository.findQuestionIdsByUserIdAndQuestionIds(userId, questionIds);
    }
}

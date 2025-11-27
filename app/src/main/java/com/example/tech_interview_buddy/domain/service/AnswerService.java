package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.repository.AnswerRepository;
import com.example.tech_interview_buddy.domain.repository.QuestionRepository;
import com.example.tech_interview_buddy.domain.repository.QuestionRepositoryImpl;
import org.springframework.context.annotation.Lazy;
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

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public Answer createAnswer(Long questionId, Long userId, String content) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        // User 엔티티는 ID만 필요하므로 간단히 생성
        User user = User.builder().build();
        // Reflection으로 ID 설정 (또는 Repository에서 조회)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set user id", e);
        }
        
        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(content)
                .build();

        return answerRepository.save(answer);
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
        return answerRepository.findByUserIdAndQuestionId(userId, questionId);
    }

    public Set<Long> getSolvedQuestionIdsByUserAndQuestions(Long userId, java.util.List<Long> questionIds) {
        return answerRepository.findQuestionIdsByUserIdAndQuestionIds(userId, questionIds);
    }
}

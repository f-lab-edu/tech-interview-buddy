package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.CreateAnswerRequest;
import com.example.tech_interview_buddy.dto.request.UpdateAnswerRequest;
import com.example.tech_interview_buddy.dto.response.AnswerResponse;
import com.example.tech_interview_buddy.repository.AnswerRepository;
import com.example.tech_interview_buddy.repository.QuestionRepositoryImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final UserService userService;
    private final QuestionRepositoryImpl questionRepositoryImpl;

    public AnswerService(AnswerRepository answerRepository, @Lazy QuestionService questionService, UserService userService, QuestionRepositoryImpl questionRepositoryImpl) {
        this.answerRepository = answerRepository;
        this.questionService = questionService;
        this.userService = userService;
        this.questionRepositoryImpl = questionRepositoryImpl;
    }

    @Transactional
    public AnswerResponse createAnswer(Long questionId, CreateAnswerRequest request) {
        String currentUsername = getCurrentUsername();
        Optional<User> currentUser = userService.findByUsername(currentUsername);

        Question question = questionService.findById(questionId);

        // 기존 답변이 있는지 확인
        Optional<Answer> existingAnswer = answerRepository.findByUserIdAndQuestionId(
            currentUser.get().getId(), questionId);
        
        if (existingAnswer.isPresent()) {
            // 기존 답변이 있으면 업데이트 (중복 생성 방지)
            Answer answer = existingAnswer.get();
            answer.updateContent(request.getContent());
            return AnswerResponse.from(answer);
        }

        // 기존 답변이 없으면 새로 생성
        Answer answer = Answer.builder()
            .user(currentUser.get())
            .question(question)
            .content(request.getContent())
            .build();

        answer = answerRepository.save(answer);
        return AnswerResponse.from(answer);
    }

    @Transactional
    public AnswerResponse updateAnswer(Long questionId, Long answerId, UpdateAnswerRequest request) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        Answer answer = answerOpt.get();

        answer.updateContent(request.getContent());
        return AnswerResponse.from(answer);
    }
    
    public Optional<Answer> findByUserIdAndQuestionId(Long userId, Long questionId) {
        return answerRepository.findByUserIdAndQuestionId(userId, questionId);
    }
    
    public boolean isQuestionSolvedByUser(Long questionId, Long userId) {
        return answerRepository.findByUserIdAndQuestionId(userId, questionId).isPresent();
    }
    
    public Optional<Answer> getMyAnswer(Long questionId, Long userId) {
        return answerRepository.findByUserIdAndQuestionId(userId, questionId);
    }
    
    /**
     * 사용자가 풀은 문제 ID들을 배치로 조회 (성능 최적화)
     */
    public Set<Long> getSolvedQuestionIdsByUser(Long userId) {
        return answerRepository.findQuestionIdsByUserId(userId);
    }
    
    /**
     * 특정 질문들에 대해 사용자가 풀었는지 확인 (성능 최적화)
     * 전체 조회가 아닌 특정 질문들에 대해서만 조회
     */
    public Set<Long> getSolvedQuestionIdsByUserAndQuestions(Long userId, java.util.List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        return answerRepository.findQuestionIdsByUserIdAndQuestionIds(userId, questionIds);
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

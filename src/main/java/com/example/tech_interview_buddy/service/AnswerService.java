package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.CreateAnswerRequest;
import com.example.tech_interview_buddy.dto.request.UpdateAnswerRequest;
import com.example.tech_interview_buddy.dto.response.AnswerResponse;
import com.example.tech_interview_buddy.repository.AnswerRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final UserService userService;

    public AnswerService(AnswerRepository answerRepository, @Lazy QuestionService questionService, UserService userService) {
        this.answerRepository = answerRepository;
        this.questionService = questionService;
        this.userService = userService;
    }

    @Transactional
    public AnswerResponse createAnswer(Long questionId, CreateAnswerRequest request) {
        String currentUsername = getCurrentUsername();
        Optional<User> currentUser = userService.findByUsername(currentUsername);

        Question question = questionService.findById(questionId);

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

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

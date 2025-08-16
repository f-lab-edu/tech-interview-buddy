package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.CreateAnswerRequest;
import com.example.tech_interview_buddy.dto.request.UpdateAnswerRequest;
import com.example.tech_interview_buddy.dto.response.AnswerResponse;
import com.example.tech_interview_buddy.repository.AnswerRepository;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnswerResponse createAnswer(Long questionId, CreateAnswerRequest request) {
        String currentUsername = getCurrentUsername();
        Optional<User> currentUser = userRepository.findByUsername(currentUsername);

        Optional<Question> question = questionRepository.findById(questionId);

        Answer answer = Answer.builder()
            .user(currentUser.get())
            .question(question.get())
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

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

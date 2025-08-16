package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.AnswerRequest;
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
    public AnswerResponse createAnswer(Long questionId, AnswerRequest request) {
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

        Answer answer = createOrUpdateAnswer(currentUser, question, request.getContent());
        return AnswerResponse.from(answer);
    }
    
    private Answer createOrUpdateAnswer(User user, Question question, String content) {
        Optional<Answer> existingAnswer = answerRepository.findByUserIdAndQuestionId(
            user.getId(), question.getId());
            
        if (existingAnswer.isPresent()) {
            Answer answer = existingAnswer.get();
            answer.updateContent(content);
            return answer;
        } else {
            Answer answer = Answer.builder()
                .user(user)
                .question(question)
                .content(content)
                .build();
            return answerRepository.save(answer);
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

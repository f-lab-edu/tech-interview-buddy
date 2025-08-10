package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.repository.AnswerRepository;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public Answer createAnswer(Long userId, Long questionId, String content) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));
        
        Optional<Answer> existingAnswer = answerRepository.findByUserAndQuestion(user, question);
        if (existingAnswer.isPresent()) {
            Answer answer = existingAnswer.get();
            answer.updateContent(content);
            return answer;
        }
        
        Answer answer = Answer.builder()
            .user(user)
            .question(question)
            .content(content)
            .build();
        
        return answerRepository.save(answer);
    }
}

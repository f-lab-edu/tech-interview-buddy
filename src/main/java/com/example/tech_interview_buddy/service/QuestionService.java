package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Tag;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.repository.AnswerRepository;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    public Page<Question> findAllQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public Page<Question> findByCategory(Category category, Pageable pageable) {
        return questionRepository.findByCategory(category, pageable);
    }

    public Page<Question> findByKeyword(String keyword, Pageable pageable) {
        return questionRepository.findByKeyword(keyword, pageable);
    }

    public Page<Question> findByTags(List<String> tagNames, Pageable pageable) {
        return questionRepository.findByTags(tagNames, pageable);
    }

    public Page<Question> findBySolvedStatus(Boolean isSolved, Pageable pageable) {
        return questionRepository.findByIsSolved(isSolved, pageable);
    }

    @Transactional
    public void markQuestionAsSolved(Long id) {
        Question question = findById(id);
        question.markAsSolved();
    }

    public Page<QuestionListResponse> findAllQuestionsAsDTO(Pageable pageable) {
        User currentUser = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return questionRepository.findAll(pageable)
            .map(question -> convertToListResponse(question, currentUser));
    }

    public Page<QuestionListResponse> findByCategoryAsDTO(Category category, Pageable pageable) {
        User currentUser = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return questionRepository.findByCategory(category, pageable)
            .map(question -> convertToListResponse(question, currentUser));
    }

    public Page<QuestionListResponse> findByKeywordAsDTO(String keyword, Pageable pageable) {
        User currentUser = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return questionRepository.findByKeyword(keyword, pageable)
            .map(question -> convertToListResponse(question, currentUser));
    }

    public Page<QuestionListResponse> findByTagsAsDTO(List<String> tagNames, Pageable pageable) {
        User currentUser = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return questionRepository.findByTags(tagNames, pageable)
            .map(question -> convertToListResponse(question, currentUser));
    }

    public QuestionDetailResponse findQuestionWithMyAnswer(Long questionId) {
        Question question = findById(questionId);
        User currentUser = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return convertToDetailResponse(question, currentUser);
    }

    private QuestionListResponse convertToListResponse(Question question, User currentUser) {
        boolean isSolved = answerRepository
            .findByUserIdAndQuestionId(currentUser.getId(), question.getId())
            .isPresent();

        return QuestionListResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .tags(question.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
            .isSolved(isSolved)
            .createdAt(question.getCreatedAt())
            .build();
    }

    private QuestionDetailResponse convertToDetailResponse(Question question, User currentUser) {
        Optional<Answer> myAnswer = answerRepository
            .findByUserIdAndQuestionId(currentUser.getId(), question.getId());

        QuestionDetailResponse.MyAnswerResponse myAnswerResponse = null;
        if (myAnswer.isPresent()) {
            Answer answer = myAnswer.get();
            myAnswerResponse = QuestionDetailResponse.MyAnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .build();
        }

        return QuestionDetailResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .tags(question.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .myAnswer(myAnswerResponse)
            .build();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Tag;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final UserService userService;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    /**
     * 동적 쿼리를 사용한 통합 검색 메서드
     * 기존의 여러 메서드들을 하나로 통합
     */
    public Page<QuestionListResponse> searchQuestions(QuestionSearchRequest searchRequest) {
        User currentUser = userService.getCurrentUser();

        Pageable pageable = createPageable(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            searchRequest.getSort(), 
            searchRequest.getDirection()
        );

        return questionRepository.searchQuestions(searchRequest, pageable, currentUser.getId())
            .map(question -> convertToListResponse(question, currentUser));
    }

    public QuestionDetailResponse findQuestionWithMyAnswer(Long questionId) {
        Question question = findById(questionId);
        User currentUser = userService.getCurrentUser();
        return convertToDetailResponse(question, currentUser);
    }

    @Transactional
    public void markQuestionAsSolved(Long id) {
        Question question = findById(id);
        question.markAsSolved();
    }

    private QuestionListResponse convertToListResponse(Question question, User currentUser) {
        boolean isSolved = answerService.isQuestionSolvedByUser(question.getId(), currentUser.getId());

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
        Optional<Answer> myAnswer = answerService.getMyAnswer(question.getId(), currentUser.getId());

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

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }
}
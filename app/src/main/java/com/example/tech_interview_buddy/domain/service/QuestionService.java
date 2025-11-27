package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.QuestionTag;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import com.example.tech_interview_buddy.domain.repository.QuestionRepository;
import com.example.tech_interview_buddy.domain.repository.QuestionTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final QuestionCountService questionCountService;
    private final QuestionTagRepository questionTagRepository;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    /**
     * 동적 쿼리를 사용한 통합 검색 메서드
     * Domain 기반 검색 결과 반환 (DTO 변환은 API 계층에서 처리)
     */
    public Page<QuestionSearchResult> searchQuestions(QuestionSearchSpec spec, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = createPageable(spec);

        // 캐싱된 총 개수 조회 (1시간마다 갱신)
        long totalCount = questionCountService.getTotalCount(spec, currentUserId);
        long countTime = System.currentTimeMillis();
        System.out.println("COUNT 조회 시간 (캐시): " + (countTime - startTime) + "ms");

        // 질문 조회
        Page<Question> questions = questionRepository.searchQuestions(spec, pageable, currentUserId);
        long queryTime = System.currentTimeMillis();
        System.out.println("DB 쿼리 시간: " + (queryTime - countTime) + "ms");

        // Question ID 추출 (결과 20개만)
        List<Long> questionIds = questions.getContent().stream()
            .map(Question::getId)
            .toList();

        // 🚀 성능 최적화: 조회된 20개 질문에 대해서만 Solved 여부 확인
        Set<Long> solvedQuestionIds = answerService.getSolvedQuestionIdsByUserAndQuestions(
            currentUserId, 
            questionIds
        );
        long solvedIdsTime = System.currentTimeMillis();
        System.out.println("Solved IDs 조회 시간 (최적화): " + (solvedIdsTime - queryTime) + "ms");
        
        // QuestionTag 배치 조회 (IN 쿼리 1번) - 120만 레코드 JOIN 제거!
        List<QuestionTag> questionTags = Collections.emptyList();
        if (!questionIds.isEmpty()) {
            questionTags = questionTagRepository.findByQuestionIdsWithTag(questionIds);
        }
        long tagTime = System.currentTimeMillis();
        System.out.println("태그 배치 조회 시간: " + (tagTime - solvedIdsTime) + "ms");
        
        // Question ID별로 태그 그룹화 (메모리에서)
        Map<Long, List<String>> questionTagMap = questionTags.stream()
            .collect(Collectors.groupingBy(
                qt -> qt.getQuestion().getId(),
                Collectors.mapping(qt -> qt.getTag().getName(), Collectors.toList())
            ));

        // QuestionSearchResult로 변환
        List<QuestionSearchResult> content = questions.getContent().stream()
            .map(question -> QuestionSearchResult.builder()
                .question(question)
                .isSolved(solvedQuestionIds.contains(question.getId()))
                .tags(questionTagMap.getOrDefault(question.getId(), Collections.emptyList()))
                .build())
            .toList();
        
        Page<QuestionSearchResult> result = new PageImpl<>(content, pageable, totalCount);
        
        long conversionTime = System.currentTimeMillis();
        System.out.println("결과 변환 시간: " + (conversionTime - tagTime) + "ms");
        System.out.println("Service 총 시간: " + (conversionTime - startTime) + "ms");

        return result;
    }

    public QuestionWithAnswer findQuestionWithAnswer(Long questionId, Long userId) {
        Question question = findById(questionId);
        Optional<Answer> answer = answerService.getMyAnswer(questionId, userId);
        
        return QuestionWithAnswer.builder()
            .question(question)
            .answer(answer.orElse(null))
            .build();
    }

    @Transactional
    public void markQuestionAsSolved(Long id) {
        Question question = findById(id);
        question.markAsSolved();
    }

    @Transactional
    public Question createQuestion(String content, Category category) {
        Question question = Question.builder()
            .content(content)
            .category(category)
            .build();
        
        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Long questionId, String content, Category category) {
        Question question = findById(questionId);
        question.updateContent(content);
        question.updateCategory(category);
        return question;
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    private Pageable createPageable(QuestionSearchSpec spec) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(spec.getSortDirection()) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        return PageRequest.of(
            spec.getPage(), 
            spec.getSize(), 
            Sort.by(sortDirection, spec.getSortField())
        );
    }
}
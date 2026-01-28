package com.example.tech_interview_buddy.domain.service;

import com.example.tech_interview_buddy.app.client.RecommendServiceClient;
import com.example.tech_interview_buddy.app.dto.external.RecommendRequest;
import com.example.tech_interview_buddy.app.dto.external.RecommendResponse;
import com.example.tech_interview_buddy.app.dto.external.RecommendedQuestion;
import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.QuestionTag;
import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import com.example.tech_interview_buddy.domain.repository.QuestionRepository;
import com.example.tech_interview_buddy.domain.repository.QuestionTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final QuestionCountService questionCountService;
    private final QuestionTagRepository questionTagRepository;
    private final RecommendServiceClient recommendServiceClient;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    public Page<QuestionSearchResult> searchQuestions(QuestionSearchSpec spec, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = createPageable(spec);

        // 캐싱된 총 개수 조회 (1시간마다 갱신)
        long totalCount = questionCountService.getTotalCount(spec, currentUserId);
        long countTime = System.currentTimeMillis();
        log.debug("COUNT 조회 시간 (캐시): {}ms", countTime - startTime);

        Page<Question> questions = questionRepository.searchQuestions(spec, pageable, currentUserId);
        long queryTime = System.currentTimeMillis();
        log.debug("DB 쿼리 시간: {}ms", queryTime - countTime);

        List<Long> questionIds = extractQuestionIds(questions);

        Set<Long> solvedQuestionIds = answerService.getSolvedQuestionIdsByUserAndQuestions(
            currentUserId,
            questionIds
        );
        long solvedIdsTime = System.currentTimeMillis();
        log.debug("Solved IDs 조회 시간 (최적화): {}ms", solvedIdsTime - queryTime);

        List<QuestionTag> questionTags = Collections.emptyList();
        if (!questionIds.isEmpty()) {
            questionTags = questionTagRepository.findByQuestionIdsWithTag(questionIds);
        }
        long tagTime = System.currentTimeMillis();
        log.debug("태그 배치 조회 시간: {}ms", tagTime - solvedIdsTime);

        Map<Long, List<String>> questionTagMap = questionTags.stream()
            .collect(Collectors.groupingBy(
                qt -> qt.getQuestion().getId(),
                Collectors.mapping(qt -> qt.getTag().getName(), Collectors.toList())
            ));

        List<QuestionSearchResult> content = questions.getContent().stream()
            .map(question -> QuestionSearchResult.builder()
                .question(question)
                .isSolved(solvedQuestionIds.contains(question.getId()))
                .tags(questionTagMap.getOrDefault(question.getId(), Collections.emptyList()))
                .build())
            .toList();

        Page<QuestionSearchResult> result = new PageImpl<>(content, pageable, totalCount);

        long conversionTime = System.currentTimeMillis();
        log.debug("결과 변환 시간: {}ms", conversionTime - tagTime);
        log.info("질문 검색 완료 - 총 {}개 결과, 총 소요 시간: {}ms",
            result.getTotalElements(), conversionTime - startTime);

        return result;
    }

    public QuestionSearchWithRecommendResult searchQuestionsWithRecommend(
            QuestionSearchSpec spec,
            Long currentUserId,
            Category category,
            List<String> tags) {

        Page<QuestionSearchResult> searchResults = searchQuestions(spec, currentUserId);

        RecommendRequest recommendRequest = RecommendRequest.builder()
            .category(category != null ? category.toString() : null)
            .tags(tags)
            .build();

        RecommendResponse recommendResponse = recommendServiceClient.callRecommendService(recommendRequest);

        List<RecommendedQuestion> recommendedQuestions =
            Optional.ofNullable(recommendResponse)
                .map(RecommendResponse::getRecommendedQuestions)
                .orElse(Collections.emptyList());

        return QuestionSearchWithRecommendResult.builder()
            .searchResults(searchResults)
            .recommendedQuestions(recommendedQuestions)
            .build();
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


    private List<Long> extractQuestionIds(Page<Question> questions) {
        return questions.getContent().stream()
            .map(Question::getId)
            .toList();
    }
}
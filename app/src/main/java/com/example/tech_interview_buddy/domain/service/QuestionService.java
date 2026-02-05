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
        Pageable pageable = createPageable(spec);

        // 캐싱된 총 개수 조회 (1시간마다 갱신)
        long totalCount = questionCountService.getTotalCount(spec, currentUserId);

        Page<Question> questions = questionRepository.searchQuestions(spec, pageable, currentUserId);

        List<Long> questionIds = extractQuestionIds(questions);

        Set<Long> solvedQuestionIds = getSolvedQuestionIds(currentUserId, questionIds);

        Map<Long, List<String>> questionTagMap = getQuestionTagMap(questionIds);

        List<QuestionSearchResult> content = convertToQuestionSearchResults(
            questions.getContent(),
            solvedQuestionIds,
            questionTagMap
        );

        Page<QuestionSearchResult> result = new PageImpl<>(content, pageable, totalCount);

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

    public RecommendResponse getRecommendResponse(Category category, List<String> tags) {
        RecommendRequest recommendRequest = RecommendRequest.builder()
            .category(category != null ? category.toString() : null)
            .tags(tags)
            .build();

        return recommendServiceClient.callRecommendService(recommendRequest);
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

    private Set<Long> getSolvedQuestionIds(Long currentUserId, List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptySet();
        }

        return answerService.getSolvedQuestionIdsByUserAndQuestions(
            currentUserId,
            questionIds
        );
    }

     private Map<Long, List<String>> getQuestionTagMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<QuestionTag> questionTags = questionTagRepository.findByQuestionIdsWithTag(questionIds);

        return questionTags.stream()
            .collect(Collectors.groupingBy(
                qt -> qt.getQuestion().getId(),
                Collectors.mapping(qt -> qt.getTag().getName(), Collectors.toList())
            ));
    }

    private List<QuestionSearchResult> convertToQuestionSearchResults(
            List<Question> questions,
            Set<Long> solvedQuestionIds,
            Map<Long, List<String>> questionTagMap) {
        return questions.stream()
            .map(question -> QuestionSearchResult.builder()
                .question(question)
                .isSolved(solvedQuestionIds.contains(question.getId()))
                .tags(questionTagMap.getOrDefault(question.getId(), Collections.emptyList()))
                .build())
            .toList();
    }
}
package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.QuestionTag;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.dto.enums.SortDirection;
import com.example.tech_interview_buddy.dto.enums.SortField;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.QuestionTagRepository;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final UserService userService;
    private final QuestionCountService questionCountService;
    private final QuestionTagRepository questionTagRepository;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    /**
     * 동적 쿼리를 사용한 통합 검색 메서드
     * 기존의 여러 메서드들을 하나로 통합
     * 전통적인 페이지네이션 지원 (캐싱된 COUNT 사용)
     */
    public Page<QuestionListResponse> searchQuestions(QuestionSearchRequest searchRequest) {
        long startTime = System.currentTimeMillis();
        
        // JWT 필터에서 이미 조회한 User 엔티티 재사용 (중복 DB 조회 방지)
        User currentUser = getCurrentUserFromRequest();
        long userTime = System.currentTimeMillis();
        System.out.println("사용자 조회 시간: " + (userTime - startTime) + "ms");

        Pageable pageable = createPageable(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            searchRequest.getSort(), 
            searchRequest.getDirection()
        );

        // 캐싱된 총 개수 조회 (1시간마다 갱신)
        long totalCount = questionCountService.getTotalCount(searchRequest, currentUser.getId());
        long countTime = System.currentTimeMillis();
        System.out.println("COUNT 조회 시간 (캐시): " + (countTime - userTime) + "ms");

        // 질문 조회
        Page<Question> questions = questionRepository.searchQuestions(searchRequest, pageable, currentUser.getId());
        long queryTime = System.currentTimeMillis();
        System.out.println("DB 쿼리 시간: " + (queryTime - countTime) + "ms");

        // Question ID 추출 (결과 20개만)
        List<Long> questionIds = questions.getContent().stream()
            .map(Question::getId)
            .toList();

        // 🚀 성능 최적화: 조회된 20개 질문에 대해서만 Solved 여부 확인
        Set<Long> solvedQuestionIds = answerService.getSolvedQuestionIdsByUserAndQuestions(
            currentUser.getId(), 
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

        // PageImpl에 정확한 totalElements 전달 (태그 포함)
        List<QuestionListResponse> content = questions.getContent().stream()
            .map(question -> convertToListResponse(
                question, 
                solvedQuestionIds,
                questionTagMap.getOrDefault(question.getId(), Collections.emptyList())
            ))
            .toList();
        
        Page<QuestionListResponse> result = new PageImpl<>(content, pageable, totalCount);
        
        long conversionTime = System.currentTimeMillis();
        System.out.println("DTO 변환 시간: " + (conversionTime - tagTime) + "ms");
        System.out.println("Service 총 시간: " + (conversionTime - startTime) + "ms");

        return result;
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

    @Transactional
    public QuestionDetailResponse createQuestion(QuestionCreateRequest request) {
        Question question = Question.builder()
            .content(request.getContent())
            .category(request.getCategory())
            .build();
        
        Question savedQuestion = questionRepository.save(question);
        User currentUser = userService.getCurrentUser();
        return convertToDetailResponse(savedQuestion, currentUser);
    }


    @Transactional
    public QuestionDetailResponse updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = findById(questionId);

        question.updateContent(request.getContent());
        question.updateCategory(request.getCategory());

        User currentUser = userService.getCurrentUser();
        return convertToDetailResponse(question, currentUser);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    private QuestionListResponse convertToListResponse(Question question, Set<Long> solvedQuestionIds, List<String> tags) {
        boolean isSolvedByUser = solvedQuestionIds.contains(question.getId());
        return QuestionListResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .isSolved(isSolvedByUser)
            .createdAt(question.getCreatedAt())
            .tags(tags != null ? tags : Collections.emptyList())
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
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .myAnswer(myAnswerResponse)
            .build();
    }

    private Pageable createPageable(int page, int size, SortField sort, SortDirection direction) {
        Sort.Direction sortDirection = direction.toSortDirection();
        return PageRequest.of(page, size, Sort.by(sortDirection, sort.getFieldName()));
    }
    
    /**
     * JWT 필터에서 이미 조회한 User 엔티티를 Request에서 가져오기
     * 중복 DB 조회를 방지하여 성능 최적화
     */
    private User getCurrentUserFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            User user = (User) request.getAttribute("currentUser");
            
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 fallback
        }
        
        // fallback: 기존 방식으로 조회
        return userService.getCurrentUser();
    }
}
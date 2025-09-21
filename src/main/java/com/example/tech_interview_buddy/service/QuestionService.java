package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Answer;
import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.dto.enums.SortDirection;
import com.example.tech_interview_buddy.dto.enums.SortField;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.tech_interview_buddy.dto.projection.QuestionSimpleProjection;

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
        long totalCount = getTotalCount(searchRequest, currentUser.getId());
        long countTime = System.currentTimeMillis();
        System.out.println("COUNT 조회 시간 (캐시): " + (countTime - userTime) + "ms");

        // 질문 조회
        Page<Question> questions = questionRepository.searchQuestions(searchRequest, pageable, currentUser.getId());
        long queryTime = System.currentTimeMillis();
        System.out.println("DB 쿼리 시간: " + (queryTime - countTime) + "ms");

        // 사용자가 풀은 문제 ID들을 배치로 조회 (성능 최적화)
        Set<Long> solvedQuestionIds = answerService.getSolvedQuestionIdsByUser(currentUser.getId());
        long solvedIdsTime = System.currentTimeMillis();
        System.out.println("Solved IDs 조회 시간: " + (solvedIdsTime - queryTime) + "ms");

        // PageImpl에 정확한 totalElements 전달
        List<QuestionListResponse> content = questions.getContent().stream()
            .map(question -> convertToListResponse(question, solvedQuestionIds))
            .toList();
        
        Page<QuestionListResponse> result = new PageImpl<>(content, pageable, totalCount);
        
        long conversionTime = System.currentTimeMillis();
        System.out.println("DTO 변환 시간: " + (conversionTime - solvedIdsTime) + "ms");
        System.out.println("Service 총 시간: " + (conversionTime - startTime) + "ms");

        return result;
    }

    /**
     * JPA/Hibernate 오버헤드 진단을 위한 DTO Projection 메서드
     * 엔티티 로딩 대신 간단한 DTO로 조회하여 성능 측정
     */
    public Page<QuestionListResponse> searchQuestionsWithProjection(QuestionSearchRequest searchRequest) {
        // JWT 필터에서 이미 조회한 User 엔티티 재사용 (중복 DB 조회 방지)
        User currentUser = getCurrentUserFromRequest();

        Pageable pageable = createPageable(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            searchRequest.getSort(), 
            searchRequest.getDirection()
        );

        // DTO Projection으로 JPA 엔티티 로딩 오버헤드 제거
        Page<QuestionSimpleProjection> projections = questionRepository.searchQuestionsWithProjection(searchRequest, pageable, currentUser.getId());

        return projections.map(projection -> convertProjectionToListResponse(projection, currentUser));
    }

    /**
     * 검색 조건에 맞는 질문의 총 개수를 반환합니다.
     * 결과는 1시간 동안 캐싱되어 성능을 최적화합니다.
     *
     * @param searchRequest 검색 조건
     * @param currentUserId 현재 사용자 ID
     * @return 질문 총 개수
     */
    @Cacheable(
        value = "questionCount",
        key = "#searchRequest.category + ':' + #searchRequest.keyword + ':' + #searchRequest.tags + ':' + #searchRequest.isSolved + ':' + #currentUserId"
    )
    public long getTotalCount(QuestionSearchRequest searchRequest, Long currentUserId) {
        return questionRepository.countQuestions(searchRequest, currentUserId);
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

    private QuestionListResponse convertProjectionToListResponse(QuestionSimpleProjection projection, User currentUser) {
        return QuestionListResponse.builder()
            .id(projection.getId())
            .content(projection.getContent())
            .category(projection.getCategory())
            .isSolved(false) // isSolved 계산 제거로 성능 최적화
            .createdAt(projection.getCreatedAt())
            .build();
    }

    

    private QuestionListResponse convertToListResponse(Question question, Set<Long> solvedQuestionIds) {
        boolean isSolvedByUser = solvedQuestionIds.contains(question.getId());
        return QuestionListResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .isSolved(isSolvedByUser)
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
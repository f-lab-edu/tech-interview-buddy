package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.client.RecommendServiceClient;
import com.example.tech_interview_buddy.app.dto.external.RecommendRequest;
import com.example.tech_interview_buddy.app.dto.external.RecommendResponse;
import com.example.tech_interview_buddy.app.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.app.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.app.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.app.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.app.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.app.dto.response.QuestionSearchResponse;
import com.example.tech_interview_buddy.app.mapper.QuestionMapper;
import com.example.tech_interview_buddy.domain.service.QuestionService;
import com.example.tech_interview_buddy.domain.service.QuestionSearchResult;
import com.example.tech_interview_buddy.domain.service.QuestionWithAnswer;
import com.example.tech_interview_buddy.domain.service.QuestionSearchWithRecommendResult;
import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.app.dto.enums.SortDirection;
import com.example.tech_interview_buddy.app.dto.enums.SortField;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final RecommendServiceClient recommendServiceClient;
    private final QuestionMapper questionMapper;

    @PostMapping("/search")
    public QuestionSearchResponse searchQuestions(@RequestBody QuestionSearchRequest searchRequest, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        
        QuestionSearchSpec spec = toSpec(searchRequest);
        
        QuestionSearchWithRecommendResult result =
            questionService.searchQuestionsWithRecommend(
                spec,
                currentUserId,
                searchRequest.getCategory(),
                searchRequest.getTags()
            );
        
        List<QuestionListResponse> contents = questionMapper.toQuestionListResponseList(
            result.getSearchResults().getContent()
        );
        
        // QuestionSearchResponse 생성
        return QuestionSearchResponse.builder()
            .contents(contents)
            .recommendations(result.getRecommendedQuestions())
            .build();
    }

    @GetMapping
    public Page<QuestionListResponse> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        // 기본 조회용 - 단순한 페이징만 제공
        QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
            .page(page)
            .size(size)
            .sort(SortField.ID)
            .direction(SortDirection.ASC)
            .build();
        
        Long currentUserId = getCurrentUserId(request);
        
        // DTO → Spec 변환
        QuestionSearchSpec spec = toSpec(searchRequest);
        
        Page<QuestionSearchResult> results = questionService.searchQuestions(spec, currentUserId);
        
        // Domain → DTO 변환
        return results.map(questionMapper::toQuestionListResponse);
    }

    @GetMapping("/{id}")
    public QuestionDetailResponse getQuestion(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        QuestionWithAnswer result = questionService.findQuestionWithAnswer(id, currentUserId);
        
        // Domain → DTO 변환
        QuestionDetailResponse.MyAnswerResponse myAnswerResponse = null;
        if (result.getAnswer() != null) {
            myAnswerResponse = QuestionDetailResponse.MyAnswerResponse.builder()
                .id(result.getAnswer().getId())
                .content(result.getAnswer().getContent())
                .evaluation(result.getAnswer().getEvaluation())
                .createdAt(result.getAnswer().getCreatedAt())
                .updatedAt(result.getAnswer().getUpdatedAt())
                .build();
        }
        
        return QuestionDetailResponse.builder()
            .id(result.getQuestion().getId())
            .content(result.getQuestion().getContent())
            .category(result.getQuestion().getCategory())
            .createdAt(result.getQuestion().getCreatedAt())
            .updatedAt(result.getQuestion().getUpdatedAt())
            .myAnswer(myAnswerResponse)
            .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDetailResponse createQuestion(@RequestBody QuestionCreateRequest request) {
        com.example.tech_interview_buddy.domain.Question question = 
            questionService.createQuestion(request.getContent(), request.getCategory());
        
        RecommendRequest recommendRequest = RecommendRequest.builder()
            .category(request.getCategory() != null ? request.getCategory().toString() : null)
            .tags(request.getTags())
            .build();
        
        RecommendResponse recommendResponse = recommendServiceClient.callRecommendService(recommendRequest);
        
        // Domain → DTO 변환
        return QuestionDetailResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .recommendData(recommendResponse) // 외부 서비스 데이터 추가
            .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDetailResponse updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionUpdateRequest request) {
        com.example.tech_interview_buddy.domain.Question question = 
            questionService.updateQuestion(id, request.getContent(), request.getCategory());
        
        // Domain → DTO 변환
        return QuestionDetailResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .category(question.getCategory())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }
    

    private QuestionSearchSpec toSpec(QuestionSearchRequest request) {
        return QuestionSearchSpec.builder()
            .category(request.getCategory())
            .keyword(request.getKeyword())
            .tags(request.getTags())
            .isSolved(request.getIsSolved())
            .page(request.getPage())
            .size(request.getSize())
            .sortField(request.getSort() != null ? request.getSort().getFieldName() : "id")
            .sortDirection(request.getDirection() != null ? request.getDirection().getDirection() : "asc")
            .build();
    }
    
    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getId() : null;
    }


}

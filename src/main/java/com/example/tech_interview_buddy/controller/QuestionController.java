package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.dto.request.TagRequest;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.service.QuestionService;
import com.example.tech_interview_buddy.dto.enums.SortDirection;
import com.example.tech_interview_buddy.dto.enums.SortField;
import com.example.tech_interview_buddy.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/search")
    public Page<QuestionListResponse> searchQuestions(@RequestBody QuestionSearchRequest searchRequest) {
        return questionService.searchQuestions(searchRequest);
    }

    @GetMapping
    public Page<QuestionListResponse> getQuestions(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean isSolved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SortField sort,
            @RequestParam(required = false) SortDirection direction) {
        
        QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
            .category(category)
            .keyword(keyword)
            .tags(tags)
            .isSolved(isSolved)
            .page(page)
            .size(size)
            .sort(sort != null ? sort : SortField.ID)
            .direction(direction != null ? direction : SortDirection.ASC)
            .build();
            
        return questionService.searchQuestions(searchRequest);
    }

    @GetMapping("/{id}")
    public QuestionDetailResponse getQuestion(@PathVariable Long id) {
        return questionService.findQuestionWithMyAnswer(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDetailResponse createQuestion(@RequestBody QuestionCreateRequest request) {
        return questionService.createQuestion(request);
    }

    @PostMapping("/{id}/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDetailResponse addTagsToQuestion(@PathVariable Long id, @RequestBody List<String> tagNames) {
        return questionService.addTagsToQuestion(id, tagNames);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDetailResponse updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionUpdateRequest request) {
        return questionService.updateQuestion(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @PostMapping("/{id}/tags/single")
    @PreAuthorize("hasRole('ADMIN')")
    public void addTagToQuestion(
            @PathVariable Long id,
            @RequestBody TagRequest request) {
        questionService.addTagToQuestion(id, request);
    }

    @DeleteMapping("/{id}/tags/{tagName}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeTagFromQuestion(
            @PathVariable Long id,
            @PathVariable String tagName) {
        questionService.removeTagFromQuestion(id, tagName);
    }
} 
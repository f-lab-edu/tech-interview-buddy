package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public Page<QuestionListResponse> getQuestions(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) Category category,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> tags,
        @RequestParam(required = false) Boolean isSolved) {

        if (isSolved != null) {
            return isSolved
                ? questionService.findSolvedByCurrentUser(pageable)
                : questionService.findUnsolvedByCurrentUser(pageable);
        }

        if (category != null) {
            return questionService.findByCategoryAsDTO(category, pageable);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return questionService.findByKeywordAsDTO(keyword.trim(), pageable);
        }

        if (tags != null && !tags.isEmpty()) {
            return questionService.findByTagsAsDTO(tags, pageable);
        }

        return questionService.findAllQuestionsAsDTO(pageable);
    }

    @GetMapping("/{id}")
    public QuestionDetailResponse getQuestion(@PathVariable Long id) {
        return questionService.findQuestionWithMyAnswer(id);
    }
} 
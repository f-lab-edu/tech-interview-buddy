package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.QuestionListResponse;
import com.example.tech_interview_buddy.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/search")
    public Page<QuestionListResponse> searchQuestions(@RequestBody QuestionSearchRequest searchRequest) {
        return questionService.searchQuestions(searchRequest);
    }

    @GetMapping("/{id}")
    public QuestionDetailResponse getQuestion(@PathVariable Long id) {
        return questionService.findQuestionWithMyAnswer(id);
    }
} 
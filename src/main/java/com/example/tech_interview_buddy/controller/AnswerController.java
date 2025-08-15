package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.dto.request.AnswerRequest;
import com.example.tech_interview_buddy.dto.response.AnswerResponse;
import com.example.tech_interview_buddy.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/{questionId}/answers")
    public AnswerResponse createAnswer(@PathVariable Long questionId,
        @RequestBody AnswerRequest request) {
        return answerService.createAnswer(questionId, request);
    }
} 
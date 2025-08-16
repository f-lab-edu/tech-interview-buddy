package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.dto.request.CreateAnswerRequest;
import com.example.tech_interview_buddy.dto.request.UpdateAnswerRequest;
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
        @RequestBody CreateAnswerRequest request) {
        return answerService.createAnswer(questionId, request);
    }
    
    @PutMapping("/{questionId}/answers")
    public AnswerResponse updateAnswer(@PathVariable Long questionId,
        @RequestBody UpdateAnswerRequest request) {
        return answerService.updateAnswer(questionId, request.getAnswerId(), request);
    }
} 
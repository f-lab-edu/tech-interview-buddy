package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.dto.request.CreateAnswerRequest;
import com.example.tech_interview_buddy.app.dto.request.UpdateAnswerRequest;
import com.example.tech_interview_buddy.app.dto.response.AnswerResponse;
import com.example.tech_interview_buddy.domain.service.AnswerService;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/{questionId}/answers")
    public AnswerResponse createAnswer(
            @PathVariable Long questionId,
            @RequestBody CreateAnswerRequest request,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        Answer answer = answerService.createAnswer(questionId, currentUserId, request.getContent());
        
        // Domain → DTO 변환
        return AnswerResponse.builder()
            .id(answer.getId())
            .content(answer.getContent())
            .createdAt(answer.getCreatedAt())
            .updatedAt(answer.getUpdatedAt())
            .build();
    }
    
    @PutMapping("/{questionId}/answers/{answerId}")
    public AnswerResponse updateAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @RequestBody UpdateAnswerRequest request,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        Answer answer = answerService.updateAnswer(answerId, currentUserId, request.getContent());
        
        // Domain → DTO 변환
        return AnswerResponse.builder()
            .id(answer.getId())
            .content(answer.getContent())
            .createdAt(answer.getCreatedAt())
            .updatedAt(answer.getUpdatedAt())
            .build();
    }
    
    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getId() : null;
    }
}

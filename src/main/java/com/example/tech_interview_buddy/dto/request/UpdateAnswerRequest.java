package com.example.tech_interview_buddy.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class UpdateAnswerRequest {
    private Long answerId;
    private String content;
    
    @Builder
    public UpdateAnswerRequest(Long answerId, String content) {
        this.answerId = answerId;
        this.content = content;
    }
}

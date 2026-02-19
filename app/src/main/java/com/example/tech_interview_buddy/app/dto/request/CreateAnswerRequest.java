package com.example.tech_interview_buddy.app.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class CreateAnswerRequest {
    private String content;
    
    @Builder
    public CreateAnswerRequest(String content) {
        this.content = content;
    }
}

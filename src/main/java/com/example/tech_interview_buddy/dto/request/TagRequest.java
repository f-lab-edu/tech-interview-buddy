package com.example.tech_interview_buddy.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class TagRequest {
    private String name;
    
    @Builder
    public TagRequest(String name) {
        this.name = name;
    }
}

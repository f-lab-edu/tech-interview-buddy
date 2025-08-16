package com.example.tech_interview_buddy.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAnswerRequest {
    private String content;
}

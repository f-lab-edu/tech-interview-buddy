package com.example.tech_interview_buddy.app.dto.response;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class UserLoginResponse {
    private String token;
}

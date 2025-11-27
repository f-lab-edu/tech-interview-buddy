package com.example.tech_interview_buddy.app.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class UserLoginRequest {
    private String username;
    private String password;
    
    @Builder
    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
package com.example.tech_interview_buddy.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    
    @Builder
    public UserCreateRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
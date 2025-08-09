package com.example.tech_interview_buddy.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private String username;
    private String email;
}

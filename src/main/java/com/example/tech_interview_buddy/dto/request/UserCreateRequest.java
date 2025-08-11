package com.example.tech_interview_buddy.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
}
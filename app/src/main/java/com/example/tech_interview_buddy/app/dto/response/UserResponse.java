package com.example.tech_interview_buddy.app.dto.response;

import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final UserRole role;

    @Builder
    public UserResponse(Long id, String username, String email, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}

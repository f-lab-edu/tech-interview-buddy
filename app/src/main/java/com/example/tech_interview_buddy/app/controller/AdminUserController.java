package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.dto.response.UserResponse;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userService.findAll();
        return users.stream()
            .map(user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
            .collect(Collectors.toList());
    }
}

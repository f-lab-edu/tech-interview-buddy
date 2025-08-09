package com.example.tech_interview_buddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.tech_interview_buddy.service.UserService;
import com.example.tech_interview_buddy.dto.request.UserCreateRequest;
import com.example.tech_interview_buddy.dto.request.UserLoginRequest;
import com.example.tech_interview_buddy.domain.User;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getUsers() {
        return "users";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateRequest userCreateRequest) {
        try {
            Long userId = userService.save(userCreateRequest);
            return ResponseEntity.ok().body("User registered successfully with ID: " + userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        try {
            User user = userService.login(userLoginRequest);
            return ResponseEntity.ok().body("Login successful for user: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}

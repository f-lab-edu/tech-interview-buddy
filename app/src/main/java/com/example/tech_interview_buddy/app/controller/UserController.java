package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.config.JwtTokenProvider;
import com.example.tech_interview_buddy.domain.service.UserService;
import com.example.tech_interview_buddy.app.dto.request.UserCreateRequest;
import com.example.tech_interview_buddy.app.dto.request.UserLoginRequest;
import com.example.tech_interview_buddy.app.dto.response.UserLoginResponse;
import com.example.tech_interview_buddy.app.dto.response.UserResponse;
import com.example.tech_interview_buddy.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/me")
    public UserResponse getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return UserResponse.builder()
            .id(currentUser.getId())
            .username(currentUser.getUsername())
            .email(currentUser.getEmail())
            .role(currentUser.getRole())
            .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userService.findAll();
        
        // Domain → DTO 변환
        return users.stream()
            .map(user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
            .collect(Collectors.toList());
    }

    @PostMapping
    public void registerUser(@RequestBody UserCreateRequest userCreateRequest) {
        userService.createUser(
            userCreateRequest.getUsername(),
            userCreateRequest.getEmail(),
            userCreateRequest.getPassword()
        );
    }

    @PostMapping("/login")
    public UserLoginResponse loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return UserLoginResponse.builder()
            .token(jwt)
            .build();
    }

    @PostMapping("/{userId}/grant-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse grantAdminRole(@PathVariable Long userId) {
        User user = userService.grantAdminRole(userId);
        
        // Domain → DTO 변환
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    @PostMapping("/{userId}/revoke-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse revokeAdminRole(@PathVariable Long userId) {
        User user = userService.revokeAdminRole(userId);
        
        // Domain → DTO 변환
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}

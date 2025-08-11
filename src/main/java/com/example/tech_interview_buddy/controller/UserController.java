package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.config.JwtTokenProvider;
import com.example.tech_interview_buddy.service.UserService;
import com.example.tech_interview_buddy.dto.request.UserCreateRequest;
import com.example.tech_interview_buddy.dto.request.UserLoginRequest;
import com.example.tech_interview_buddy.dto.response.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @GetMapping
    public String getUsers() {
        return "users";
    }

    @PostMapping
    public void registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userService.save(userCreateRequest);
    }

    @PostMapping("/login")
    public UserLoginResponse loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new UserLoginResponse(jwt);
    }
}

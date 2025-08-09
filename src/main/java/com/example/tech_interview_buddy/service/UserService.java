package com.example.tech_interview_buddy.service;

import org.springframework.stereotype.Service;
import com.example.tech_interview_buddy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.tech_interview_buddy.dto.request.UserCreateRequest;
import com.example.tech_interview_buddy.dto.request.UserLoginRequest;
import com.example.tech_interview_buddy.domain.User;
import jakarta.transaction.Transactional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public void save(UserCreateRequest userCreateRequest) {
        validateUserRegistration(userCreateRequest);

        User user = User.builder()
            .username(userCreateRequest.getUsername())
            .email(userCreateRequest.getEmail())
            .password(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()))
            .build();

        userRepository.save(user);
        return;
    }

    public User login(UserLoginRequest userLoginRequest) {
        User user = userRepository.findByUsername(userLoginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!bCryptPasswordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    private void validateUserRegistration(UserCreateRequest userCreateRequest) {
        if (userRepository.findByUsername(userCreateRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + userCreateRequest.getUsername());
        }

        if (userRepository.findByEmail(userCreateRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + userCreateRequest.getEmail());
        }

        if (userCreateRequest.getUsername() == null || userCreateRequest.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty");
        }

        if (userCreateRequest.getEmail() == null || userCreateRequest.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }

        if (userCreateRequest.getPassword() == null || userCreateRequest.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

    }

}

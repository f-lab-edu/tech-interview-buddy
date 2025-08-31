package com.example.tech_interview_buddy.service;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.tech_interview_buddy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.tech_interview_buddy.dto.request.UserCreateRequest;
import com.example.tech_interview_buddy.dto.response.UserResponse;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.UserRole;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;


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
        User user = User.builder()
            .username(userCreateRequest.getUsername())
            .email(userCreateRequest.getEmail())
            .password(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()))
            .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists");
        }
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }

    // Admin 기능들 - 기존 AdminService에서 이동
    @Transactional
    public UserResponse grantAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        user.updateRole(UserRole.ADMIN);
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    @Transactional
    public UserResponse revokeAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        user.updateRole(UserRole.USER);
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build())
                .collect(Collectors.toList());
    }
}

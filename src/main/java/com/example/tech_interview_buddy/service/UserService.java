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
    public Long save(UserCreateRequest userCreateRequest) {
        // 중복 검증
        validateUserRegistration(userCreateRequest);

        // 새 사용자 생성
        User user = new User();
        user.setUsername(userCreateRequest.getUsername());
        user.setEmail(userCreateRequest.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()));

        // 사용자 저장
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    public User login(UserLoginRequest userLoginRequest) {
        // 사용자명으로 사용자 찾기
        User user = userRepository.findByUsername(userLoginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 비밀번호 검증
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

        // 입력 데이터 검증
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

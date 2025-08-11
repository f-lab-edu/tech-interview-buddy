package com.example.tech_interview_buddy.service;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.tech_interview_buddy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.tech_interview_buddy.dto.request.UserCreateRequest;
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
}

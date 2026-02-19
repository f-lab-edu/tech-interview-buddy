package com.example.tech_interview_buddy.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.tech_interview_buddy.domain.repository.UserRepository;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.UserRole;
import com.example.tech_interview_buddy.common.security.PasswordEncoder;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Domain 서비스: User 도메인 로직
 * Security/Servlet 의존성 제거, PasswordEncoder 인터페이스만 사용
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists");
        }
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    /**
     * Admin 권한 부여
     * API 계층에서 userId를 받아서 처리
     */
    @Transactional
    public User grantAdminRole(Long userId) {
        User user = findById(userId);
        user.updateRole(UserRole.ADMIN);
        return user;
    }

    /**
     * Admin 권한 회수
     * API 계층에서 userId를 받아서 처리
     */
    @Transactional
    public User revokeAdminRole(Long userId) {
        User user = findById(userId);
        user.updateRole(UserRole.USER);
        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}

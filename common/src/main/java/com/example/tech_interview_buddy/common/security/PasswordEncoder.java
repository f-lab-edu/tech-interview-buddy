package com.example.tech_interview_buddy.common.security;

/**
 * Common 모듈의 PasswordEncoder 인터페이스
 * Domain에서 사용하며, 구현체는 API 모듈에서 제공
 */
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}


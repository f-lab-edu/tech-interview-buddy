package com.example.tech_interview_buddy.domain.exception;

/**
 * 이미 존재하는 리소스와 충돌할 때(예: 중복 가입) 던진다. HTTP 409로 매핑된다.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

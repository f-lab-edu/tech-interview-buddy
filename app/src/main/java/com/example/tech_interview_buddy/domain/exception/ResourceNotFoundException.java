package com.example.tech_interview_buddy.domain.exception;

/**
 * 요청한 리소스가 존재하지 않을 때 던진다. HTTP 404로 매핑된다.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

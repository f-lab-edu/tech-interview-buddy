package com.example.tech_interview_buddy.domain.service.resume;

public class ResumeAiReviewException extends RuntimeException {

    public ResumeAiReviewException(String message) {
        super(message);
    }

    public ResumeAiReviewException(String message, Throwable cause) {
        super(message, cause);
    }
}

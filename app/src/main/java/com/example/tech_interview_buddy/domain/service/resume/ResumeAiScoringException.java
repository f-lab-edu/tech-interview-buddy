package com.example.tech_interview_buddy.domain.service.resume;

public class ResumeAiScoringException extends RuntimeException {

    public ResumeAiScoringException(String message) {
        super(message);
    }

    public ResumeAiScoringException(String message, Throwable cause) {
        super(message, cause);
    }
}

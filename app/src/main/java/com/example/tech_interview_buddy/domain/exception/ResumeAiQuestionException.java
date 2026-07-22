package com.example.tech_interview_buddy.domain.exception;

public class ResumeAiQuestionException extends RuntimeException {

    public ResumeAiQuestionException(String message) {
        super(message);
    }

    public ResumeAiQuestionException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.example.tech_interview_buddy.app.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String path;
    private String timestamp;
    private Map<String, String> validationErrors;
}

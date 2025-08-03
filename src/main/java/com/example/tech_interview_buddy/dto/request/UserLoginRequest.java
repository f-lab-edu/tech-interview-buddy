package com.example.tech_interview_buddy.dto.request;

public class UserLoginRequest {
    private String username;
    private String password;

    // Default constructor
    public UserLoginRequest() {
    }

    // Constructor with parameters
    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 
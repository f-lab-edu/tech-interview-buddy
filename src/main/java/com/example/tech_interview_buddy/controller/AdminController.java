package com.example.tech_interview_buddy.controller;

import com.example.tech_interview_buddy.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.dto.request.TagRequest;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.UserResponse;
import com.example.tech_interview_buddy.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/questions")
    public ResponseEntity<QuestionDetailResponse> createQuestion(@RequestBody QuestionCreateRequest request) {
        QuestionDetailResponse response = adminService.createQuestion(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionDetailResponse> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionUpdateRequest request) {
        QuestionDetailResponse response = adminService.updateQuestion(questionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        adminService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/questions/{questionId}/tags")
    public ResponseEntity<Void> addTagToQuestion(
            @PathVariable Long questionId,
            @RequestBody TagRequest request) {
        adminService.addTagToQuestion(questionId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/questions/{questionId}/tags/{tagName}")
    public ResponseEntity<Void> removeTagFromQuestion(
            @PathVariable Long questionId,
            @PathVariable String tagName) {
        adminService.removeTagFromQuestion(questionId, tagName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{username}/grant-admin")
    public ResponseEntity<UserResponse> grantAdminRole(@PathVariable String username) {
        UserResponse response = adminService.grantAdminRole(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{username}/revoke-admin")
    public ResponseEntity<UserResponse> revokeAdminRole(@PathVariable String username) {
        UserResponse response = adminService.revokeAdminRole(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

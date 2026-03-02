package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeUploadResponse;
import com.example.tech_interview_buddy.app.service.resume.ResumeUploadService;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeUploadService resumeUploadService;

    @PostMapping
    public ResponseEntity<ResumeUploadResponse> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        Resume resume = resumeUploadService.upload(user, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResumeUploadResponse.from(resume));
    }
}

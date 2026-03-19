package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeDetailResponse;
import com.example.tech_interview_buddy.app.dto.response.resume.ResumeListItemResponse;
import com.example.tech_interview_buddy.app.dto.response.resume.ResumeUploadResponse;
import com.example.tech_interview_buddy.app.service.resume.ResumeDeleteService;
import com.example.tech_interview_buddy.app.service.resume.ResumeDetailService;
import com.example.tech_interview_buddy.app.service.resume.ResumeQueryService;
import com.example.tech_interview_buddy.app.service.resume.ResumeUploadService;
import com.example.tech_interview_buddy.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ResumeQueryService resumeQueryService;
    private final ResumeDetailService resumeDetailService;
    private final ResumeDeleteService resumeDeleteService;

    @PostMapping
    public ResponseEntity<ResumeUploadResponse> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(resumeUploadService.upload(user, file));
    }

    @GetMapping
    public ResponseEntity<Page<ResumeListItemResponse>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(resumeQueryService.listResumes(user.getId(), page, size));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeDetailResponse> detail(
            @AuthenticationPrincipal User user,
            @PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeDetailService.getDetail(resumeId, user.getId()));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long resumeId) {
        resumeDeleteService.delete(resumeId, user.getId());
        return ResponseEntity.noContent().build();
    }
}

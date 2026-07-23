package com.example.tech_interview_buddy.app.controller;

import com.example.tech_interview_buddy.app.dto.request.RecruiterRequestCreateRequest;
import com.example.tech_interview_buddy.app.dto.response.RecruiterRequestResponse;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequestStatus;
import com.example.tech_interview_buddy.domain.service.recruit.RecruiterRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruiter-requests")
@RequiredArgsConstructor
public class RecruiterRequestController {

    private final RecruiterRequestService recruiterRequestService;

    /** 채용담당자 승인 신청 (로그인한 일반 사용자) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecruiterRequestResponse apply(
            @Valid @RequestBody RecruiterRequestCreateRequest createRequest,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return RecruiterRequestResponse.from(
            recruiterRequestService.apply(userId, createRequest.companyName(), createRequest.contactInfo()));
    }

    /** 신청 목록 조회 (관리자) — status 미지정 시 전체 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RecruiterRequestResponse> list(
            @RequestParam(required = false) RecruiterRequestStatus status) {
        return recruiterRequestService.findByStatus(status).stream()
            .map(RecruiterRequestResponse::from)
            .toList();
    }

    /** 신청 승인 (관리자) */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public RecruiterRequestResponse approve(@PathVariable Long id) {
        return RecruiterRequestResponse.from(recruiterRequestService.approve(id));
    }

    /** 신청 반려 (관리자) */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public RecruiterRequestResponse reject(@PathVariable Long id) {
        return RecruiterRequestResponse.from(recruiterRequestService.reject(id));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return user.getId();
    }
}

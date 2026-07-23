package com.example.tech_interview_buddy.domain.recruit;

import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.exception.InvalidStateException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 채용담당자 승인 신청.
 * PENDING 상태에서만 승인/반려가 가능하다.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "recruiter_request")
@EntityListeners(AuditingEntityListener.class)
public class RecruiterRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_info")
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruiterRequestStatus status = RecruiterRequestStatus.PENDING;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public RecruiterRequest(User user, String companyName, String contactInfo) {
        this.user = user;
        this.companyName = companyName;
        this.contactInfo = contactInfo;
        this.status = RecruiterRequestStatus.PENDING;
    }

    public void approve(LocalDateTime processedAt) {
        requirePending();
        this.status = RecruiterRequestStatus.APPROVED;
        this.processedAt = processedAt;
    }

    public void reject(LocalDateTime processedAt) {
        requirePending();
        this.status = RecruiterRequestStatus.REJECTED;
        this.processedAt = processedAt;
    }

    private void requirePending() {
        if (this.status != RecruiterRequestStatus.PENDING) {
            throw new InvalidStateException("이미 처리된 신청입니다.");
        }
    }
}

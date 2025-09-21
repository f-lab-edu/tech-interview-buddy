package com.example.tech_interview_buddy.dto.projection;

import com.example.tech_interview_buddy.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * JPA/Hibernate 오버헤드 진단을 위한 간단한 Question DTO Projection
 * 엔티티 로딩 오버헤드를 최소화하여 성능을 측정하기 위한 클래스
 */
@Getter
@AllArgsConstructor
public class QuestionSimpleProjection {
    private Long id;
    private String content;
    private Category category;
    private Boolean isSolved;
    private LocalDateTime createdAt;
}

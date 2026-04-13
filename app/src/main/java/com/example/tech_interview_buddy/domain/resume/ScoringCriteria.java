package com.example.tech_interview_buddy.domain.resume;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScoringCriteria {
    TECHNICAL_SKILL_CLARITY("기술 스택 명확성"),
    PROJECT_EXPERIENCE("프로젝트 경험"),
    IMPACT_AND_ACHIEVEMENT("성과/임팩트"),
    ATS_COMPATIBILITY("ATS 호환성"),
    OVERALL_STRUCTURE("전체 구성/가독성");

    private final String label;
}

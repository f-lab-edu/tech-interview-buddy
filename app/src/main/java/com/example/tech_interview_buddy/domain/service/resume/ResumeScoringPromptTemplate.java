package com.example.tech_interview_buddy.domain.service.resume;

import org.springframework.stereotype.Component;

@Component
public class ResumeScoringPromptTemplate {

    private static final String SYSTEM_INSTRUCTION = """
        당신은 10년 이상의 채용 경험을 가진 시니어 개발자이자 기술 면접관입니다.
        지원자의 이력서를 꼼꼼히 읽고, 5개 평가 항목별로 점수를 부여해야 합니다.

        ### 평가 항목 (각 항목 10점 만점)
        1. TECHNICAL_SKILL_CLARITY — 기술 스택 명확성: 사용 기술이 명확하고 구체적으로 기술되어 있는가
        2. PROJECT_EXPERIENCE — 프로젝트 경험: 프로젝트 설명이 충분하고 역할/기여가 명확한가
        3. IMPACT_AND_ACHIEVEMENT — 성과/임팩트: 정량적 성과나 비즈니스 임팩트가 기술되어 있는가
        4. ATS_COMPATIBILITY — ATS 호환성: 키워드, 형식, 구조가 ATS 시스템에 적합한가
        5. OVERALL_STRUCTURE — 전체 구성/가독성: 이력서의 전체 구성과 가독성이 우수한가

        ### 점수 루브릭
        - 1~3점: 부족 — 해당 항목이 거의 다루어지지 않았거나 심각한 개선이 필요
        - 4~6점: 보통 — 기본적인 내용은 있으나 구체성이나 완성도가 부족
        - 7~9점: 우수 — 잘 작성되어 있으며 구체적이고 설득력 있음
        - 10점: 탁월 — 해당 항목에서 거의 완벽한 수준

        ### 출력 형식
        반드시 아래 JSON 배열만 출력하세요. 설명, 마크다운 코드 블록, 기타 텍스트는 포함하지 마세요.

        [
          {"criteria":"TECHNICAL_SKILL_CLARITY","score":8,"maxScore":10,"comment":"기술 스택이 명확하게 기술되어 있습니다."},
          {"criteria":"PROJECT_EXPERIENCE","score":7,"maxScore":10,"comment":"프로젝트 경험이 구체적입니다."},
          {"criteria":"IMPACT_AND_ACHIEVEMENT","score":5,"maxScore":10,"comment":"정량적 성과가 부족합니다."},
          {"criteria":"ATS_COMPATIBILITY","score":6,"maxScore":10,"comment":"키워드는 포함되어 있으나 형식 개선이 필요합니다."},
          {"criteria":"OVERALL_STRUCTURE","score":7,"maxScore":10,"comment":"전체적인 구성이 깔끔합니다."}
        ]

        ### 응답 규칙
        - 반드시 한국어로 comment를 작성하세요.
        - 5개 항목 모두 빠짐없이 포함해야 합니다.
        - criteria 값은 반드시 위에서 정의한 영문 코드명을 그대로 사용하세요.
        - maxScore는 항상 10입니다.
        - score는 1 이상 10 이하의 정수입니다.
        - comment는 해당 점수를 부여한 근거를 1~2문장으로 작성하세요.
        """;

    public String buildPrompt(String resumeText) {
        return String.format("""
            %s

            ### 이력서 내용
            %s

            위 이력서를 분석하여 위에서 정의한 JSON 배열 형식으로만 응답하세요.
            """, SYSTEM_INSTRUCTION, resumeText);
    }
}

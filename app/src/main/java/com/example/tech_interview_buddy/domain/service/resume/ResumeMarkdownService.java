package com.example.tech_interview_buddy.domain.service.resume;

import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeMarkdownService {

    private final AiAdapter aiAdapter;

    private static final String PROMPT_TEMPLATE = """
            다음은 PDF에서 추출한 이력서 텍스트입니다. 이 텍스트를 마크다운 형식으로 변환해 주세요.

            규칙:
            - 원문 내용을 절대 수정, 요약, 재작성하지 마세요. 글자 하나 바꾸지 마세요.
            - 이력서의 주요 섹션 제목(큰 구분 단위)은 ## 으로 표시
            - 하위 제목(소속, 프로젝트명, 기관명 등)은 ### 으로 표시
            - 글머리 목록은 - 으로 표시
            - 번호가 있는 목록은 1. 2. 3. 으로 표시
            - 강조되어야 할 텍스트(키워드, 기술명, 핵심 성과 등)는 **텍스트** 로 표시
            - 페이지 번호, 반복되는 머리글/바닥글은 제거
            - 마크다운 외의 설명이나 부연은 절대 포함하지 마세요. 변환된 마크다운만 출력하세요.

            이력서 텍스트:
            %s
            """;

    public void convert(Resume resume) {
        String extractedText = resume.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            log.warn("No extracted text for resumeId={}, skipping markdown conversion", resume.getId());
            return;
        }

        String prompt = PROMPT_TEMPLATE.formatted(extractedText);
        String markdown = aiAdapter.sendPrompt(prompt, 4000);

        if (markdown == null || markdown.isBlank()) {
            log.warn("Markdown conversion returned empty for resumeId={}", resume.getId());
            return;
        }

        resume.saveMarkdown(markdown);
        log.info("Markdown conversion done for resumeId={}", resume.getId());
    }
}

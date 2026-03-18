package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ResumeDetailResponse {

    private Long resumeId;
    private String filename;
    private String status;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
    private LocalDateTime analysisCompletedAt;

    private String presignedUrl;

    private String statusMessage;

    private ResumeReviewResponse review;

    private List<QuestionItem> questions;

    @Getter
    @Builder
    public static class QuestionItem {
        private Long questionId;
        private String questionText;
        private String category;
        private String difficulty;
        private String sourceSection;
        private String sourceQuote;

        public static QuestionItem from(ResumeQuestion q) {
            return QuestionItem.builder()
                .questionId(q.getId())
                .questionText(q.getQuestionText())
                .category(q.getCategory().name())
                .difficulty(q.getDifficulty().name())
                .sourceSection(q.getSourceSection())
                .sourceQuote(q.getSourceQuote())
                .build();
        }
    }
}

package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeSection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ResumeReviewResponse {

    private String summary;
    private List<SectionFeedback> strengths;
    private List<SectionFeedback> weaknesses;
    private List<SuggestionItem> suggestions;
    private List<AtsCheckItem> atsChecklist;

    @Getter
    @Builder
    @Jacksonized
    public static class SectionFeedback {
        private ResumeSection section;
        private String content;
    }

    @Getter
    @Builder
    @Jacksonized
    public static class SuggestionItem {
        private ResumeSection section;
        private String before;
        private String after;
        private String reason;
    }

    @Getter
    @Builder
    @Jacksonized
    public static class AtsCheckItem {
        private String item;
        private boolean passed;
        private String description;
    }
}

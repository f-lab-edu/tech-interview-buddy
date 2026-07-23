package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import com.example.tech_interview_buddy.domain.resume.ResumeScores;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeScoreResponse {

    private int totalScore;
    private int maxTotalScore;
    private List<ScoreDetail> scores;

    public static ResumeScoreResponse from(ResumeScores resumeScores) {
        List<ScoreDetail> scoreDetails = resumeScores.getScores().stream()
                .map(ScoreDetail::from)
                .toList();

        return ResumeScoreResponse.builder()
                .totalScore(resumeScores.getTotalScore())
                .maxTotalScore(resumeScores.getTotalMaxScore())
                .scores(scoreDetails)
                .build();
    }

    @Getter
    @Builder
    public static class ScoreDetail {
        private String criteria;
        private String criteriaLabel;
        private int score;
        private int maxScore;
        private String comment;

        public static ScoreDetail from(ResumeScore entity) {
            return ScoreDetail.builder()
                    .criteria(entity.getCriteria().name())
                    .criteriaLabel(entity.getCriteria().getLabel())
                    .score(entity.getScore())
                    .maxScore(entity.getMaxScore())
                    .comment(entity.getComment())
                    .build();
        }
    }
}

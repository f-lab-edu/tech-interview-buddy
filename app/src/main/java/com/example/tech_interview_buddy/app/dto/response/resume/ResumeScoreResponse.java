package com.example.tech_interview_buddy.app.dto.response.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeScoreResponse {

    private int totalScore;
    private int maxTotalScore;
    private List<ScoreDetail> scores;

    public static ResumeScoreResponse from(List<ResumeScore> scoreEntities) {
        int totalScore = 0;
        int maxTotalScore = 0;
        List<ScoreDetail> scoreDetails = scoreEntities.stream()
                .map(ScoreDetail::from)
                .toList();

        for (ResumeScore entity : scoreEntities) {
            totalScore += entity.getScore();
            maxTotalScore += entity.getMaxScore();
        }

        return ResumeScoreResponse.builder()
                .totalScore(totalScore)
                .maxTotalScore(maxTotalScore)
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

package com.example.tech_interview_buddy.domain.resume;

import java.util.List;

import lombok.Getter;

@Getter
public class ResumeScores {
    
    private final List<ResumeScore> scores;

    public ResumeScores(List<ResumeScore> scores) {
        this.scores = List.copyOf(scores);
    }

    public int getTotalScore() {
        return scores.stream().mapToInt(ResumeScore::getScore).sum();
    }

    public int getTotalMaxScore() {
        return scores.stream().mapToInt(ResumeScore::getMaxScore).sum();
    }

    public boolean isEmpty() {
        return scores.isEmpty();
    }
}

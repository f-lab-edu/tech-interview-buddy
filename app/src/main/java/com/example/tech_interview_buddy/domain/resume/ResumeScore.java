package com.example.tech_interview_buddy.domain.resume;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "resume_score")
public class ResumeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria", nullable = false, length = 30)
    private ScoringCriteria criteria;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Builder
    private ResumeScore(Resume resume, ScoringCriteria criteria, int score, int maxScore, String comment) {
        this.resume = resume;
        this.criteria = criteria;
        this.score = score;
        this.maxScore = maxScore;
        this.comment = comment;
    }
}

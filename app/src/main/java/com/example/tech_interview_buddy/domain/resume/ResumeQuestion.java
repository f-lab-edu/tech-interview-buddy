package com.example.tech_interview_buddy.domain.resume;

import com.example.tech_interview_buddy.common.domain.Category;
import com.example.tech_interview_buddy.common.domain.Difficulty;
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
@Table(name = "resume_question")
public class ResumeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(name = "source_section", length = 20)
    private String sourceSection;

    @Column(name = "source_quote", columnDefinition = "TEXT")
    private String sourceQuote;

    @Builder
    private ResumeQuestion(Resume resume, String questionText, Category category,
                           Difficulty difficulty, String sourceSection, String sourceQuote) {
        this.resume = resume;
        this.questionText = questionText;
        this.category = category;
        this.difficulty = difficulty;
        this.sourceSection = sourceSection;
        this.sourceQuote = sourceQuote;
    }
}

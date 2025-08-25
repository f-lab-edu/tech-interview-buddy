package com.example.tech_interview_buddy.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "question")
@EntityListeners(AuditingEntityListener.class)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private Category category;

    @Column(name = "is_solved", nullable = false)
    private Boolean isSolved = false;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionTag> questionTags = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Question(String content, Category category) {
        this.content = content;
        this.category = category;
        this.isSolved = false;
        this.questionTags = new ArrayList<>();
    }

    public void addTag(Tag tag) {
        QuestionTag questionTag = QuestionTag.builder()
            .question(this)
            .tag(tag)
            .build();
        questionTags.add(questionTag);
    }

    public List<Tag> getTags() {
        return questionTags.stream()
            .map(QuestionTag::getTag)
            .collect(Collectors.toList());
    }

    public void removeTag(Tag tag) {
        questionTags.removeIf(qt -> qt.getTag().equals(tag));
    }
    
    public void markAsSolved() {
        this.isSolved = true;
    }
    
    public void markAsUnsolved() {
        this.isSolved = false;
    }
    
    public void changeCategory(Category category) {
        this.category = category;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void setTags(List<Tag> tags) {
        this.questionTags.clear();
        
        if (tags != null) {
            tags.forEach(this::addTag);
        }
    }

    public void removeTag(String tagName) {
        questionTags.removeIf(qt -> qt.getTag().getName().equals(tagName));
    }
}

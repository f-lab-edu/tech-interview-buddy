package com.example.tech_interview_buddy.domain.repository.resume;

import static com.example.tech_interview_buddy.domain.resume.QResumeQuestion.resumeQuestion;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class ResumeQuestionRepositoryImpl implements ResumeQuestionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deleteAllByResumeId(Long resumeId) {
        new JPAQueryFactory(entityManager)
                .delete(resumeQuestion)
                .where(resumeQuestion.resume.id.eq(resumeId))
                .execute();
    }
}

package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.QQuestion;
import com.example.tech_interview_buddy.domain.QAnswer;
import com.example.tech_interview_buddy.domain.QQuestionTag;
import com.example.tech_interview_buddy.domain.QTag;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import com.querydsl.jpa.JPAExpressions;

@Repository
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Question> searchQuestions(QuestionSearchRequest searchRequest, Pageable pageable, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        
        QQuestion question = QQuestion.question;
        QAnswer answer = QAnswer.answer;
        QQuestionTag questionTag = QQuestionTag.questionTag;
        QTag tag = QTag.tag;

        JPAQuery<Question> query = queryFactory
            .selectFrom(question)
            .where(buildPredicates(question, answer, questionTag, tag, searchRequest, currentUserId));

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                switch (order.getProperty()) {
                    case "id" -> query.orderBy(order.isAscending() ? question.id.asc() : question.id.desc());
                    case "content" -> query.orderBy(order.isAscending() ? question.content.asc() : question.content.desc());
                    case "category" -> query.orderBy(order.isAscending() ? question.category.asc() : question.category.desc());
                    case "createdAt" -> query.orderBy(order.isAscending() ? question.createdAt.asc() : question.createdAt.desc());
                    case "updatedAt" -> query.orderBy(order.isAscending() ? question.updatedAt.asc() : question.updatedAt.desc());
                    default -> query.orderBy(question.id.asc());
                }
            });
        } else {
            query.orderBy(question.id.asc());
        }

        List<Question> questions = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(question.count())
            .from(question)
            .where(buildPredicates(question, answer, questionTag, tag, searchRequest, currentUserId))
            .fetchOne();

        return new PageImpl<>(questions, pageable, total != null ? total : 0);
    }

    private BooleanBuilder buildPredicates(QQuestion question, QAnswer answer, 
                                         QQuestionTag questionTag, QTag tag,
                                         QuestionSearchRequest searchRequest, Long currentUserId) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchRequest.getCategory() != null) {
            builder.and(question.category.eq(searchRequest.getCategory()));
        }

        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            builder.and(question.content.containsIgnoreCase(searchRequest.getKeyword().trim()));
        }

        if (searchRequest.getTags() != null && !searchRequest.getTags().isEmpty()) {
            builder.and(question.questionTags.any().tag.name.in(searchRequest.getTags()));
        }

        if (searchRequest.getIsSolved() != null && currentUserId != null) {
            if (searchRequest.getIsSolved()) {
                builder.and(
                    JPAExpressions.selectOne()
                        .from(answer)
                        .where(answer.question.eq(question).and(answer.user.id.eq(currentUserId)))
                        .exists()
                );
            } else {
                builder.and(
                    JPAExpressions.selectOne()
                        .from(answer)
                        .where(answer.question.eq(question).and(answer.user.id.eq(currentUserId)))
                        .notExists()
                );
            }
        }

        return builder;
    }
}
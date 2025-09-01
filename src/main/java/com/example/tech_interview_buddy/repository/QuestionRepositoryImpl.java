package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Question;

import static com.example.tech_interview_buddy.domain.QQuestion.question;
import static com.example.tech_interview_buddy.domain.QAnswer.answer;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPAExpressions;
import com.example.tech_interview_buddy.repository.util.QueryDslPredicateBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    private static final Map<String, Function<Boolean, OrderSpecifier<?>>> SORT_MAPPINGS = Map.of(
        "id", ascending -> ascending ? question.id.asc() : question.id.desc(),
        "content", ascending -> ascending ? question.content.asc() : question.content.desc(),
        "category", ascending -> ascending ? question.category.asc() : question.category.desc(),
        "createdAt", ascending -> ascending ? question.createdAt.asc() : question.createdAt.desc(),
        "updatedAt", ascending -> ascending ? question.updatedAt.asc() : question.updatedAt.desc()
    );

    @Override
    public Page<Question> searchQuestions(QuestionSearchRequest searchRequest, Pageable pageable, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Question> query = queryFactory
            .selectFrom(question)
            .where(buildPredicates(searchRequest, currentUserId));

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                Function<Boolean, OrderSpecifier<?>> sortFunction = SORT_MAPPINGS.get(order.getProperty());
                if (sortFunction != null) {
                    query.orderBy(sortFunction.apply(order.isAscending()));
                } else {
                    query.orderBy(question.id.asc());
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
            .where(buildPredicates(searchRequest, currentUserId))
            .fetchOne();

        return new PageImpl<>(questions, pageable, total != null ? total : 0);
    }

    private BooleanBuilder buildPredicates(QuestionSearchRequest searchRequest, Long currentUserId) {
        return QueryDslPredicateBuilder.newBuilder()
            .andIfNotNull(searchRequest.getCategory(), 
                category -> question.category.eq(category))
            .andIfNotBlank(searchRequest.getKeyword(), 
                keyword -> question.content.containsIgnoreCase(keyword))
            .andIfNotEmpty(searchRequest.getTags(), 
                tags -> question.questionTags.any().tag.name.in(tags))
            .andIfTrue(searchRequest.getIsSolved() != null && currentUserId != null && searchRequest.getIsSolved(), 
                () -> JPAExpressions.selectOne()
                    .from(answer)
                    .where(answer.question.eq(question).and(answer.user.id.eq(currentUserId)))
                    .exists())
            .andIfTrue(searchRequest.getIsSolved() != null && currentUserId != null && !searchRequest.getIsSolved(), 
                () -> JPAExpressions.selectOne()
                    .from(answer)
                    .where(answer.question.eq(question).and(answer.user.id.eq(currentUserId)))
                    .notExists())
            .build();
    }
}
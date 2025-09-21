package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Question;
import static com.example.tech_interview_buddy.domain.QAnswer.answer;
import static com.example.tech_interview_buddy.domain.QQuestion.question;
import static com.example.tech_interview_buddy.domain.QQuestionTag.questionTag;
import static com.example.tech_interview_buddy.domain.QTag.tag;
import com.example.tech_interview_buddy.dto.request.QuestionSearchRequest;
import com.example.tech_interview_buddy.dto.projection.QuestionSimpleProjection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

        // 태그 없이 단순 조회로 성능 최적화
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

        // COUNT 쿼리 제거로 성능 최적화 - 무한 스크롤 방식
        List<Question> questions = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        // hasNextPage 판단 후 실제 size만 반환
        boolean hasNextPage = questions.size() > pageable.getPageSize();
        if (hasNextPage) {
            questions = questions.subList(0, pageable.getPageSize());
        }

        return new PageImpl<>(questions, pageable, hasNextPage ? pageable.getOffset() + questions.size() + 1 : pageable.getOffset() + questions.size());
    }

    private BooleanBuilder buildPredicates(QuestionSearchRequest searchRequest, Long currentUserId) {
        return QueryDslPredicateBuilder.newBuilder()
            .andIfNotNull(searchRequest.getCategory(), 
                category -> question.category.eq(category))
            .andIfNotBlank(searchRequest.getKeyword(), 
                keyword -> question.content.containsIgnoreCase(keyword))
            .andIfNotEmpty(searchRequest.getTags(), 
                tags -> JPAExpressions.selectFrom(questionTag)
                    .join(questionTag.tag, tag)
                    .where(questionTag.question.id.eq(question.id)
                        .and(tag.name.in(tags)))
                    .exists())
            .andIfNotNull(searchRequest.getIsSolved(), 
                isSolved -> isSolved ? 
                    JPAExpressions.selectFrom(answer)
                        .where(answer.question.id.eq(question.id)
                            .and(answer.user.id.eq(currentUserId)))
                        .exists() :
                    JPAExpressions.selectFrom(answer)
                        .where(answer.question.id.eq(question.id)
                            .and(answer.user.id.eq(currentUserId)))
                        .notExists())
            .build();
    }
    
    /**
     * JPA/Hibernate 오버헤드 진단을 위한 DTO Projection 메서드
     * 엔티티 로딩 대신 간단한 DTO로 조회하여 성능 측정
     */
    @Override
    public Page<QuestionSimpleProjection> searchQuestionsWithProjection(QuestionSearchRequest searchRequest, Pageable pageable, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // DTO Projection으로 JPA 엔티티 로딩 오버헤드 제거
        JPAQuery<QuestionSimpleProjection> query = queryFactory
            .select(Projections.constructor(QuestionSimpleProjection.class,
                question.id,
                question.content,
                question.category,
                question.isSolved,
                question.createdAt
            ))
            .from(question)
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

        // COUNT 쿼리 제거로 성능 최적화 - 무한 스크롤 방식
        // hasNextPage 판단을 위해 size+1 개 조회
        List<QuestionSimpleProjection> projections = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        // hasNextPage 판단 후 실제 size만 반환
        boolean hasNextPage = projections.size() > pageable.getPageSize();
        if (hasNextPage) {
            projections = projections.subList(0, pageable.getPageSize());
        }

        return new PageImpl<>(projections, pageable, hasNextPage ? pageable.getOffset() + projections.size() + 1 : pageable.getOffset() + projections.size());
    }
    
    @Override
    public long countQuestions(QuestionSearchRequest searchRequest, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // searchQuestions와 동일한 WHERE 조건 사용
        JPAQuery<Long> query = queryFactory
            .select(question.count())
            .from(question)
            .where(buildPredicates(searchRequest, currentUserId));

        Long count = query.fetchOne();
        return count != null ? count : 0L;
    }
    
}
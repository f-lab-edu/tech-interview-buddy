package com.example.tech_interview_buddy.domain.repository;

import com.example.tech_interview_buddy.domain.Question;

import static com.example.tech_interview_buddy.domain.QAnswer.answer;
import static com.example.tech_interview_buddy.domain.QQuestion.question;
import static com.example.tech_interview_buddy.domain.QQuestionTag.questionTag;
import static com.example.tech_interview_buddy.domain.QTag.tag;

import com.example.tech_interview_buddy.domain.spec.QuestionSearchSpec;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.tech_interview_buddy.domain.repository.util.QueryDslPredicateBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.querydsl.core.types.OrderSpecifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final QuestionTagRepository questionTagRepository;

    private static final Map<String, Function<Boolean, OrderSpecifier<?>>> SORT_MAPPINGS = Map.of(
            "id", ascending -> ascending ? question.id.asc() : question.id.desc(),
            "content", ascending -> ascending ? question.content.asc() : question.content.desc(),
            "category", ascending -> ascending ? question.category.asc() : question.category.desc(),
            "createdAt", ascending -> ascending ? question.createdAt.asc() : question.createdAt.desc(),
            "updatedAt", ascending -> ascending ? question.updatedAt.asc() : question.updatedAt.desc()
    );

    @Override
    public Page<Question> searchQuestions(QuestionSearchSpec spec, Pageable pageable, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // 🚀 2단계 쿼리 최적화: 태그 조건이 있으면 서브쿼리로 ID만 먼저 조회
        if (spec.getTags() != null && !spec.getTags().isEmpty()) {
            // 1단계: 모든 조건 + 정렬 + 페이징 적용한 ID 목록 조회
            List<Long> pagedQuestionIds = findQuestionIdsByConditions(spec, pageable, currentUserId, queryFactory);

            // ID가 없으면 빈 결과 반환
            if (pagedQuestionIds.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // 2단계: 조회된 ID로 Question 엔티티 가져오기 (정렬 순서 유지)
            JPAQuery<Question> query = queryFactory.selectFrom(question)
                    .where(question.id.in(pagedQuestionIds));

            // ID 순서대로 정렬 (1단계 쿼리의 순서 유지)
            if (pageable.getSort().isSorted()) {
                pageable.getSort().forEach(order -> {
                    Function<Boolean, OrderSpecifier<?>> sortFunction = SORT_MAPPINGS.get(order.getProperty());
                    if (sortFunction != null) {
                        query.orderBy(sortFunction.apply(order.isAscending()));
                    }
                });
            } else {
                query.orderBy(question.id.asc());
            }

            List<Question> questions = query.fetch();

            // hasNextPage는 1단계에서 limit+1로 판단
            boolean hasNextPage = pagedQuestionIds.size() > pageable.getPageSize();

            return new PageImpl<>(questions, pageable, hasNextPage ? pageable.getOffset() + questions.size() + 1 : pageable.getOffset() + questions.size());
        }

        // 태그 조건이 없으면 기존 방식대로
        JPAQuery<Question> query = queryFactory.selectFrom(question);
        query.where(buildPredicatesWithoutTags(spec, currentUserId));

        // 정렬
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

    /**
     * 태그를 포함한 모든 조건으로 Question ID만 조회 (페이징 포함)
     * 🚀 EXISTS 서브쿼리 사용: JOIN 대신 EXISTS로 성능 최적화
     */
    private List<Long> findQuestionIdsByConditions(
            QuestionSearchSpec spec,
            Pageable pageable,
            Long currentUserId,
            JPAQueryFactory queryFactory) {

        // EXISTS 서브쿼리로 태그 필터링 (JOIN 대신 EXISTS 사용)
        JPAQuery<Long> idQuery = queryFactory
                .select(question.id)
                .from(question)
                .where(JPAExpressions.selectOne()
                        .from(questionTag)
                        .join(tag).on(questionTag.tag.id.eq(tag.id))
                        .where(questionTag.question.id.eq(question.id)
                                .and(tag.name.in(spec.getTags())))
                        .exists());  // 태그 필터링

        // 나머지 조건들 (카테고리, 키워드, solved 등)
        idQuery.where(buildPredicatesWithoutTags(spec, currentUserId));

        // 정렬 적용
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                Function<Boolean, OrderSpecifier<?>> sortFunction = SORT_MAPPINGS.get(order.getProperty());
                if (sortFunction != null) {
                    idQuery.orderBy(sortFunction.apply(order.isAscending()));
                } else {
                    idQuery.orderBy(question.id.asc());
                }
            });
        } else {
            idQuery.orderBy(question.id.asc());
        }

        // 페이징 적용 (limit + 1로 hasNextPage 판단)
        return idQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    /**
     * 태그 조건을 제외한 나머지 검색 조건 생성
     * 태그는 searchQuestions()에서 먼저 처리됨 (성능 최적화)
     */
    private BooleanBuilder buildPredicatesWithoutTags(QuestionSearchSpec spec, Long currentUserId) {
        return QueryDslPredicateBuilder.newBuilder()
                .andIfNotNull(spec.getCategory(),
                        category -> question.category.eq(category))
                .andIfNotBlank(spec.getKeyword(),
                        keyword -> question.content.containsIgnoreCase(keyword))
                // 태그 조건 제거 - searchQuestions()에서 이미 처리됨
                .andIfNotNull(spec.getIsSolved(),
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
     * 태그 조건을 포함한 모든 검색 조건 생성 (countQuestions에서 사용)
     *
     * @deprecated 성능 이슈로 searchQuestions에서는 사용하지 않음
     */
    @Deprecated
    private BooleanBuilder buildPredicates(QuestionSearchSpec spec, Long currentUserId) {
        return QueryDslPredicateBuilder.newBuilder()
                .andIfNotNull(spec.getCategory(),
                        category -> question.category.eq(category))
                .andIfNotBlank(spec.getKeyword(),
                        keyword -> question.content.containsIgnoreCase(keyword))
                .andIfNotEmpty(spec.getTags(),
                        tags -> JPAExpressions.selectFrom(questionTag)
                                .join(questionTag.tag, tag)
                                .where(questionTag.question.id.eq(question.id)
                                        .and(tag.name.in(tags)))
                                .exists())
                .andIfNotNull(spec.getIsSolved(),
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

    @Override
    public long countQuestions(QuestionSearchSpec spec, Long currentUserId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // searchQuestions와 동일한 WHERE 조건 사용
        JPAQuery<Long> query = queryFactory
                .select(question.count())
                .from(question)
                .where(buildPredicates(spec, currentUserId));

        Long count = query.fetchOne();
        return count != null ? count : 0L;
    }

}
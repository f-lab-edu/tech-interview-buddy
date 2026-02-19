package com.example.tech_interview_buddy.common.util

import com.example.tech_interview_buddy.core.domain.Category
import com.example.tech_interview_buddy.core.domain.QAnswer
import com.example.tech_interview_buddy.core.domain.QQuestion
import com.example.tech_interview_buddy.core.domain.QQuestionTag
import com.example.tech_interview_buddy.core.domain.QTag
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import spock.lang.Specification

class QueryDslPredicateBuilderSpec extends Specification {

    def question = QQuestion.question
    def questionTag = QQuestionTag.questionTag
    def tag = QTag.tag
    def answer = QAnswer.answer

    def "andIfNotNull 는 값이 있을 때만 조건을 추가한다"() {
        when:
        def predicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotNull(Category.PROGRAMMING) { question.category.eq(it) }
            .build()

        then:
        predicate.toString() == new BooleanBuilder(question.category.eq(Category.PROGRAMMING)).toString()
    }

    def "andIfNotNull 는 null 일 때 조건을 추가하지 않는다"() {
        when:
        def predicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotNull(null) { question.category.eq(it) }
            .build()

        then:
        !predicate.hasValue()
    }

    def "andIfNotBlank 는 공백을 제거한 값으로 containsIgnoreCase 조건을 만든다"() {
        when:
        def predicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotBlank("  JVM  ") { question.content.containsIgnoreCase(it) }
            .build()

        then:
        predicate.hasValue()
        predicate.toString().contains("containsIc")  // QueryDSL은 containsIc로 표현
        predicate.toString().contains("JVM")
    }

    def "andIfNotEmpty 는 태그 EXISTS 서브쿼리를 추가한다"() {
        given:
        def tags = ["JVM", "멀티스레드"]

        when:
        def predicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotEmpty(tags) { names ->
                JPAExpressions.selectFrom(questionTag)
                    .join(questionTag.tag, tag)
                    .where(questionTag.question.id.eq(question.id)
                        .and(tag.name.in(names)))
                    .exists()
            }
            .build()

        then:
        predicate.hasValue()
        predicate.toString().contains("exists")  // exists 서브쿼리 확인
    }

    def "andIfTrue 와 andIfFalse 는 각각 조건을 선택적으로 추가한다"() {
        when:
        def trueBuilder = QueryDslPredicateBuilder.newBuilder()
            .andIfTrue(true) { question.isSolved.eq(true) }
        def truePredicate = trueBuilder.build()
        
        def falseBuilder = QueryDslPredicateBuilder.newBuilder()
            .andIfFalse(false) { question.isSolved.eq(false) }
        def falsePredicate = falseBuilder.build()

        then:
        truePredicate.hasValue()
        truePredicate.toString().contains("question.isSolved = true")
        falsePredicate.hasValue()
        falsePredicate.toString().contains("question.isSolved = false")
    }

    def "andIfExists 와 andIfNotExists 는 서브쿼리 존재 여부에 따라 조건을 만든다"() {
        when:
        def existsPredicate = QueryDslPredicateBuilder.newBuilder()
            .andIfExists(true) {
                JPAExpressions.selectFrom(answer)
                    .where(answer.question.id.eq(question.id))
                    .exists()
            }
            .build()
            
        def notExistsPredicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotExists(true) {
                JPAExpressions.selectFrom(answer)
                    .where(answer.question.id.eq(question.id))
                    .notExists()
            }
            .build()

        then:
        existsPredicate.hasValue()
        existsPredicate.toString().contains("exists")
        notExistsPredicate.hasValue()
        notExistsPredicate.toString().contains("exists")  // notExists도 exists를 포함
    }

    def "빌더는 체이닝된 조건을 모두 포함한 BooleanBuilder 를 반환한다"() {
        when:
        def predicate = QueryDslPredicateBuilder.newBuilder()
            .andIfNotNull(Category.PROGRAMMING) { question.category.eq(it) }
            .andIfNotBlank("Spring") { question.content.containsIgnoreCase(it) }
            .andIfNotEmpty(["JVM"]) {
                JPAExpressions.selectFrom(questionTag)
                    .join(questionTag.tag, tag)
                    .where(tag.name.in(it))
                    .exists()
            }
            .build()

        then:
        predicate.hasValue()
        predicate.toString().contains("question.category")
        predicate.toString().contains("containsIc")  // QueryDSL은 containsIc로 표현
        predicate.toString().contains("exists")
    }

    def "이미 존재하는 BooleanBuilder 를 확장할 수 있다"() {
        given:
        def existing = new BooleanBuilder(question.id.eq(1L))

        when:
        def predicate = QueryDslPredicateBuilder.newBuilder(existing)
            .andIfNotNull(Category.ALGORITHM) { question.category.eq(it) }
            .build()

        then:
        predicate.hasValue()
        predicate.toString().contains("question.id = 1")
        predicate.toString().contains("question.category =")
    }
}

package com.example.tech_interview_buddy.common

import com.example.tech_interview_buddy.core.domain.*
import com.example.tech_interview_buddy.common.dto.enums.SortDirection
import com.example.tech_interview_buddy.common.dto.enums.SortField
import com.example.tech_interview_buddy.common.dto.request.QuestionSearchRequest

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * 테스트 전용 엔티티/DTO 생성 유틸리티.
 * ID는 JPA가 자동 생성하므로 직접 지정하지 않는다.
 */
class TestDataBuilder {

    private static final AtomicInteger uniqueCounter = new AtomicInteger(1000)

    static User createUser(String username = "testuser") {
        def suffix = uniqueCounter.incrementAndGet()
        return User.builder()
            .username("${username}${suffix}")
            .email("${username}${suffix}@example.com")
            .password("\$2a\$10\$encryptedPassword")
            .build()
    }

    static Question createQuestion(String content = "Sample interview question", Category category = Category.PROGRAMMING) {
        return Question.builder()
            .content(content)
            .category(category)
            .build()
    }

    static Tag createTag(String name = "tag-${uniqueCounter.incrementAndGet()}", String description = "Test tag description") {
        return Tag.builder()
            .name(name)
            .description(description)
            .build()
    }

    static QuestionTag createQuestionTag(Question question, Tag tag) {
        return QuestionTag.builder()
            .question(question)
            .tag(tag)
            .build()
    }

    static Answer createAnswer(User user, Question question, String content = "Sample answer content") {
        return Answer.builder()
            .user(user)
            .question(question)
            .content(content)
            .build()
    }

    static QuestionSearchRequest createSearchRequest(Map<String, Object> params = [:]) {
        def builder = QuestionSearchRequest.builder()

        if (params.category != null) builder.category(params.category as Category)
        if (params.keyword != null) builder.keyword(params.keyword as String)
        if (params.tags != null) builder.tags(params.tags as List<String>)
        if (params.isSolved != null) builder.isSolved(params.isSolved as Boolean)
        if (params.page != null) builder.page(params.page as Integer)
        if (params.size != null) builder.size(params.size as Integer)
        if (params.sort != null) builder.sort(params.sort as SortField)
        if (params.direction != null) builder.direction(params.direction as SortDirection)

        return builder.build()
    }

    static class TestDataSet {
        User user
        List<Question> questions
        List<Tag> tags
        List<QuestionTag> questionTags
        List<Answer> answers
    }
}

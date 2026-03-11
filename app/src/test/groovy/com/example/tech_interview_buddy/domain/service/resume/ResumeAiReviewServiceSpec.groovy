package com.example.tech_interview_buddy.domain.service.resume

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository
import com.example.tech_interview_buddy.domain.resume.Resume
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Subject

class ResumeAiReviewServiceSpec extends Specification {

    AiAdapter aiAdapter = Mock()
    ResumeReviewPromptTemplate promptTemplate = Mock()
    ResumeRepository resumeRepository = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    @Subject
    ResumeAiReviewService service = new ResumeAiReviewService(
            aiAdapter, promptTemplate, objectMapper, resumeRepository
    )

    static final String VALID_JSON = """
        {
          "summary": "전반적으로 잘 작성된 이력서입니다.",
          "strengths": [
            { "section": "SKILLS", "content": "기술 스택이 명확합니다." }
          ],
          "weaknesses": [
            { "section": "EXPERIENCE", "content": "경력 기술이 부족합니다." }
          ],
          "suggestions": [
            {
              "section": "PROJECT",
              "before": "프로젝트를 진행했습니다.",
              "after": "Spring Boot와 JPA를 활용하여 REST API를 설계하고 구현했습니다.",
              "reason": "구체적인 기술과 역할을 명시하면 설득력이 높아집니다."
            }
          ],
          "atsChecklist": [
            { "item": "키워드 포함 여부", "passed": true, "description": "주요 기술 키워드가 포함되어 있습니다." }
          ]
        }
    """

    def makeResume(String extractedText = "이력서 본문") {
        def resume = Mock(Resume)
        resume.getId() >> 1L
        resume.getExtractedText() >> extractedText
        return resume
    }

    // === generateAndSave 성공 케이스 ===

    def "AI가 유효한 JSON을 반환하면 파싱 후 Resume에 저장한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> VALID_JSON

        when:
        service.generateAndSave(resume)

        then:
        1 * resume.saveReviewResult(
                { it.contains("전반적으로") },
                "전반적으로 잘 작성된 이력서입니다."
        )
        1 * resumeRepository.save(resume)
    }

    def "extractedText를 프롬프트에 사용한다"() {
        given:
        def resume = makeResume("이력서 본문 텍스트")
        promptTemplate.buildPrompt("이력서 본문 텍스트") >> "prompt"
        aiAdapter.sendPrompt(_) >> VALID_JSON

        when:
        service.generateAndSave(resume)

        then:
        1 * promptTemplate.buildPrompt("이력서 본문 텍스트")
    }

    // === 재시도 케이스 ===

    def "첫 번째 AI 응답 파싱 실패 시 1회 재시도 후 성공하면 저장한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"

        when:
        service.generateAndSave(resume)

        then:
        2 * aiAdapter.sendPrompt(_) >>> ["invalid json", VALID_JSON]
        1 * resume.saveReviewResult(_, _)
        1 * resumeRepository.save(resume)
    }

    def "AI 응답이 null일 때 1회 재시도 후 성공하면 저장한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"

        when:
        service.generateAndSave(resume)

        then:
        2 * aiAdapter.sendPrompt(_) >>> [null, VALID_JSON]
        1 * resume.saveReviewResult(_, _)
    }

    // === 실패 케이스 ===

    def "두 번 모두 파싱 실패하면 ResumeAiReviewException을 던진다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> "invalid json"

        when:
        service.generateAndSave(resume)

        then:
        thrown(ResumeAiReviewException)
        2 * aiAdapter.sendPrompt(_)
        0 * resume.saveReviewResult(_, _)
        0 * resumeRepository.save(_)
    }

    def "두 번 모두 null 응답이면 ResumeAiReviewException을 던진다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> null

        when:
        service.generateAndSave(resume)

        then:
        thrown(ResumeAiReviewException)
        2 * aiAdapter.sendPrompt(_)
    }

    // === JSON 파싱 검증 ===

    def "파싱된 리뷰에 strengths, weaknesses, suggestions, atsChecklist가 포함된다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> VALID_JSON

        String capturedReviewJson = null
        resume.saveReviewResult(_, _) >> { args -> capturedReviewJson = args[0] }

        when:
        service.generateAndSave(resume)

        then:
        capturedReviewJson != null
        def parsed = objectMapper.readValue(capturedReviewJson, Map)
        parsed.strengths.size() == 1
        parsed.weaknesses.size() == 1
        parsed.suggestions.size() == 1
        parsed.atsChecklist.size() == 1
        parsed.strengths[0].section == "SKILLS"
    }

    def "JSON 앞뒤에 마크다운 코드블록이 있어도 파싱에 성공한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> "```json\n${VALID_JSON}\n```"

        when:
        service.generateAndSave(resume)

        then:
        1 * resume.saveReviewResult(_, "전반적으로 잘 작성된 이력서입니다.")
    }
}

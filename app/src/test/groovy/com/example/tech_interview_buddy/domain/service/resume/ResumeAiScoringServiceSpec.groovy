package com.example.tech_interview_buddy.domain.service.resume

import com.example.tech_interview_buddy.domain.repository.resume.ResumeScoreRepository
import com.example.tech_interview_buddy.domain.resume.Resume
import com.example.tech_interview_buddy.domain.resume.ResumeScore
import com.example.tech_interview_buddy.domain.resume.ScoringCriteria
import com.example.tech_interview_buddy.domain.service.ai.AiAdapter
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Subject

class ResumeAiScoringServiceSpec extends Specification {

    AiAdapter aiAdapter = Mock()
    ResumeScoringPromptTemplate promptTemplate = Mock()
    ResumeScoreRepository scoreRepository = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    @Subject
    ResumeAiScoringService service = new ResumeAiScoringService(
            aiAdapter, promptTemplate, objectMapper, scoreRepository
    )

    static final String VALID_JSON = """
        [
          {"criteria":"TECHNICAL_SKILL_CLARITY","score":8,"maxScore":10,"comment":"기술 스택이 명확합니다."},
          {"criteria":"PROJECT_EXPERIENCE","score":7,"maxScore":10,"comment":"프로젝트 경험이 구체적입니다."},
          {"criteria":"IMPACT_AND_ACHIEVEMENT","score":5,"maxScore":10,"comment":"정량적 성과가 부족합니다."},
          {"criteria":"ATS_COMPATIBILITY","score":6,"maxScore":10,"comment":"키워드는 포함되어 있습니다."},
          {"criteria":"OVERALL_STRUCTURE","score":7,"maxScore":10,"comment":"전체적인 구성이 깔끔합니다."}
        ]
    """

    def makeResume(String extractedText = "이력서 본문") {
        def resume = Mock(Resume)
        resume.getId() >> 1L
        resume.getExtractedText() >> extractedText
        return resume
    }

    // === generateAndSave 성공 케이스 ===

    def "AI가 유효한 JSON을 반환하면 파싱 후 점수를 저장한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> VALID_JSON

        when:
        service.generateAndSave(resume)

        then:
        1 * scoreRepository.saveAll({ List<ResumeScore> scores ->
            scores.size() == 5 &&
            scores[0].criteria == ScoringCriteria.TECHNICAL_SKILL_CLARITY &&
            scores[0].score == 8 &&
            scores[1].criteria == ScoringCriteria.PROJECT_EXPERIENCE &&
            scores[1].score == 7 &&
            scores[2].criteria == ScoringCriteria.IMPACT_AND_ACHIEVEMENT &&
            scores[2].score == 5 &&
            scores[3].criteria == ScoringCriteria.ATS_COMPATIBILITY &&
            scores[3].score == 6 &&
            scores[4].criteria == ScoringCriteria.OVERALL_STRUCTURE &&
            scores[4].score == 7
        })
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
        1 * scoreRepository.saveAll(_)
    }

    def "AI 응답이 null일 때 1회 재시도 후 성공하면 저장한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"

        when:
        service.generateAndSave(resume)

        then:
        2 * aiAdapter.sendPrompt(_) >>> [null, VALID_JSON]
        1 * scoreRepository.saveAll(_)
    }

    // === 실패 케이스 ===

    def "두 번 모두 파싱 실패하면 ResumeAiScoringException을 던진다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> "invalid json"

        when:
        service.generateAndSave(resume)

        then:
        thrown(ResumeAiScoringException)
        2 * aiAdapter.sendPrompt(_)
        0 * scoreRepository.saveAll(_)
    }

    def "두 번 모두 null 응답이면 ResumeAiScoringException을 던진다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> null

        when:
        service.generateAndSave(resume)

        then:
        thrown(ResumeAiScoringException)
        2 * aiAdapter.sendPrompt(_)
    }

    // === 점수 범위 검증 ===

    def "점수가 0-10 범위를 벗어나면 클램핑된다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> """
            [
              {"criteria":"TECHNICAL_SKILL_CLARITY","score":-1,"maxScore":10,"comment":"테스트"},
              {"criteria":"PROJECT_EXPERIENCE","score":15,"maxScore":10,"comment":"테스트"},
              {"criteria":"IMPACT_AND_ACHIEVEMENT","score":5,"maxScore":10,"comment":"테스트"},
              {"criteria":"ATS_COMPATIBILITY","score":0,"maxScore":10,"comment":"테스트"},
              {"criteria":"OVERALL_STRUCTURE","score":10,"maxScore":10,"comment":"테스트"}
            ]
        """

        when:
        service.generateAndSave(resume)

        then:
        1 * scoreRepository.saveAll({ List<ResumeScore> scores ->
            scores[0].score == 0 &&
            scores[1].score == 10 &&
            scores[2].score == 5 &&
            scores[3].score == 0 &&
            scores[4].score == 10
        })
    }

    // === 마크다운 래핑 처리 ===

    def "JSON 앞뒤에 마크다운 코드블록이 있어도 파싱에 성공한다"() {
        given:
        def resume = makeResume()
        promptTemplate.buildPrompt(_) >> "prompt"
        aiAdapter.sendPrompt(_) >> "```json\n${VALID_JSON}\n```"

        when:
        service.generateAndSave(resume)

        then:
        1 * scoreRepository.saveAll({ List<ResumeScore> scores ->
            scores.size() == 5
        })
    }
}

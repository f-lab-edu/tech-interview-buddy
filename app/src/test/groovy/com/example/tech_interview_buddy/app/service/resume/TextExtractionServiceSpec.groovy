package com.example.tech_interview_buddy.app.service.resume

import spock.lang.Specification
import spock.lang.Subject

class TextExtractionServiceSpec extends Specification {

    def pdfTextExtractor = Mock(PdfTextExtractor)
    def docxTextExtractor = Mock(DocxTextExtractor)

    @Subject
    def service = new TextExtractionService(pdfTextExtractor, docxTextExtractor)

    def "PDF 파일에서 텍스트를 정상 추출한다"() {
        given:
        def inputStream = Mock(InputStream)
        pdfTextExtractor.extract(inputStream) >> "이력서 내용입니다"

        when:
        def result = service.extract(inputStream, "application/pdf")

        then:
        result == "이력서 내용입니다"
    }

    def "DOCX 파일에서 텍스트를 정상 추출한다"() {
        given:
        def inputStream = Mock(InputStream)
        def mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        docxTextExtractor.extract(inputStream) >> "DOCX 이력서 내용"

        when:
        def result = service.extract(inputStream, mimeType)

        then:
        result == "DOCX 이력서 내용"
    }

    def "지원하지 않는 파일 형식이면 예외를 던진다"() {
        given:
        def inputStream = Mock(InputStream)

        when:
        service.extract(inputStream, "image/png")

        then:
        def e = thrown(TextExtractionException)
        e.message.contains("지원하지 않는 파일 형식")
    }

    def "추출된 텍스트가 비어있으면 예외를 던진다 - #scenario"() {
        given:
        def inputStream = Mock(InputStream)
        pdfTextExtractor.extract(inputStream) >> text

        when:
        service.extract(inputStream, "application/pdf")

        then:
        def e = thrown(TextExtractionException)
        e.message.contains("텍스트를 추출할 수 없는 파일")

        where:
        scenario       | text
        "null"         | null
        "빈 문자열"     | ""
        "공백만 있음"   | "   "
    }

    def "텍스트가 50,000자를 초과하면 예외를 던진다"() {
        given:
        def inputStream = Mock(InputStream)
        pdfTextExtractor.extract(inputStream) >> "가" * 50_001

        when:
        service.extract(inputStream, "application/pdf")

        then:
        def e = thrown(TextExtractionException)
        e.message.contains("텍스트가 너무 많은 파일")
    }

    def "텍스트가 정확히 50,000자이면 정상 처리된다"() {
        given:
        def inputStream = Mock(InputStream)
        def text = "가" * 50_000
        pdfTextExtractor.extract(inputStream) >> text

        when:
        def result = service.extract(inputStream, "application/pdf")

        then:
        result == text
    }

    def "IOException 발생 시 TextExtractionException으로 변환한다"() {
        given:
        def inputStream = Mock(InputStream)
        pdfTextExtractor.extract(inputStream) >> { throw new IOException("읽기 실패") }

        when:
        service.extract(inputStream, "application/pdf")

        then:
        def e = thrown(TextExtractionException)
        e.message.contains("텍스트를 추출하는 중 오류")
        e.cause instanceof IOException
    }
}

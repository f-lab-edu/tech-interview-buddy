package com.example.tech_interview_buddy.app.service.resume

import org.apache.poi.xwpf.usermodel.XWPFDocument
import spock.lang.Specification
import spock.lang.Subject

class DocxTextExtractorSpec extends Specification {

    @Subject
    def extractor = new DocxTextExtractor()

    def "텍스트가 포함된 DOCX에서 텍스트를 추출한다"() {
        given:
        def docxBytes = createDocxWithText("이력서 내용입니다.")
        def inputStream = new ByteArrayInputStream(docxBytes)

        when:
        def result = extractor.extract(inputStream)

        then:
        result.contains("이력서 내용입니다.")
    }

    def "빈 DOCX에서는 빈 텍스트를 반환한다"() {
        given:
        def docxBytes = createEmptyDocx()
        def inputStream = new ByteArrayInputStream(docxBytes)

        when:
        def result = extractor.extract(inputStream)

        then:
        result.isBlank()
    }

    def "잘못된 데이터를 넣으면 예외가 발생한다"() {
        given:
        def inputStream = new ByteArrayInputStream("not a docx".getBytes())

        when:
        extractor.extract(inputStream)

        then:
        thrown(Exception)
    }

    private byte[] createDocxWithText(String text) {
        def baos = new ByteArrayOutputStream()
        def document = new XWPFDocument()
        document.createParagraph().createRun().setText(text)
        document.write(baos)
        document.close()
        return baos.toByteArray()
    }

    private byte[] createEmptyDocx() {
        def baos = new ByteArrayOutputStream()
        def document = new XWPFDocument()
        document.write(baos)
        document.close()
        return baos.toByteArray()
    }
}

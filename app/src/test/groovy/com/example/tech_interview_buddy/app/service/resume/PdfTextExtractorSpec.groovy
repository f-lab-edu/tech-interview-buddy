package com.example.tech_interview_buddy.app.service.resume

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import spock.lang.Specification
import spock.lang.Subject

class PdfTextExtractorSpec extends Specification {

    @Subject
    def extractor = new PdfTextExtractor()

    def "텍스트가 포함된 PDF에서 텍스트를 추출한다"() {
        given:
        def pdfBytes = createPdfWithText("Resume text content")
        def inputStream = new ByteArrayInputStream(pdfBytes)

        when:
        def result = extractor.extract(inputStream)

        then:
        result.contains("Resume text content")
    }

    def "빈 PDF에서는 빈 텍스트를 반환한다"() {
        given:
        def pdfBytes = createEmptyPdf()
        def inputStream = new ByteArrayInputStream(pdfBytes)

        when:
        def result = extractor.extract(inputStream)

        then:
        result.isBlank()
    }

    def "잘못된 데이터를 넣으면 IOException이 발생한다"() {
        given:
        def inputStream = new ByteArrayInputStream("not a pdf".getBytes())

        when:
        extractor.extract(inputStream)

        then:
        thrown(IOException)
    }

    private byte[] createPdfWithText(String text) {
        def baos = new ByteArrayOutputStream()
        def document = new PDDocument()
        def page = new PDPage()
        document.addPage(page)

        def contentStream = new PDPageContentStream(document, page)
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 12)
        contentStream.newLineAtOffset(50, 700)
        contentStream.showText(text)
        contentStream.endText()
        contentStream.close()

        document.save(baos)
        document.close()
        return baos.toByteArray()
    }

    private byte[] createEmptyPdf() {
        def baos = new ByteArrayOutputStream()
        def document = new PDDocument()
        document.addPage(new PDPage())
        document.save(baos)
        document.close()
        return baos.toByteArray()
    }
}

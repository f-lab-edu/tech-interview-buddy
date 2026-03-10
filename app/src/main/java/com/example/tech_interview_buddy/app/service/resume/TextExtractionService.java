package com.example.tech_interview_buddy.app.service.resume;

import java.io.InputStream;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextExtractionService {

    private static final int MAX_TEXT_LENGTH = 50_000;
    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final PdfTextExtractor pdfTextExtractor;
    private final DocxTextExtractor docxTextExtractor;

    public String extract(InputStream inputStream, String mimeType) {
        if (!SUPPORTED_MIME_TYPES.contains(mimeType)) {
            throw new TextExtractionException("지원하지 않는 파일 형식입니다: " + mimeType);
        }

        String text;
        try {
            text = switch (mimeType) {
                case "application/pdf" -> pdfTextExtractor.extract(inputStream);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    docxTextExtractor.extract(inputStream);
                default -> throw new IllegalStateException("Unreachable: unsupported mimeType=" + mimeType);
            };
        } catch (Exception e) {
            throw new TextExtractionException("파일에서 텍스트를 추출하는 중 오류가 발생했습니다.", e);
        }

        if (text == null || text.isBlank()) {
            throw new TextExtractionException("텍스트를 추출할 수 없는 파일입니다. (이미지 기반 PDF 등)");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            throw new TextExtractionException("텍스트가 너무 많은 파일입니다. 이력서 길이를 줄인 후 업로드해 주세요.");
        }

        log.info("Text extracted - {} characters, mimeType: {}", text.length(), mimeType);
        return text;
    }
}

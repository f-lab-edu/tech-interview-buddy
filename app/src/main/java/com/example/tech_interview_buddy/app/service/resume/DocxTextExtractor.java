package com.example.tech_interview_buddy.app.service.resume;

import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DocxTextExtractor {

    public String extract(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            log.info("DOCX text extracted - {} characters", text.length());
            return text.trim();
        }
    }
}

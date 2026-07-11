package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.app.service.notification.NotificationCreationService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiQuestionService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiReviewService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiScoringService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeMarkdownService;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisOrchestrator {

    private final ResumeRepository resumeRepository;
    private final ResumeStorageService storageService;
    private final TextExtractionService textExtractionService;
    private final ResumeAiReviewService aiReviewService;
    private final ResumeAiScoringService aiScoringService;
    private final ResumeAiQuestionService aiQuestionService;
    private final ResumeMarkdownService markdownService;
    private final NotificationCreationService notificationCreationService;
    private final Executor taskExecutor;

    /**
     * 이력서 분석 파이프라인을 비동기로 실행한다.
     * 분석은 대부분 LLM API 호출(I/O)이므로 전역 스레드풀 크기가 동시 실행 상한을 결정한다.
     * 사용자당 이력서는 최대 개수가 제한되어 있어, 한 사용자가 슬롯을 독점할 위험은 사실상 없다.
     */
    public void analyze(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));
        taskExecutor.execute(() -> runPipeline(resume));
    }

    private void runPipeline(Resume resume) {
        log.info("Analysis started for resumeId={}", resume.getId());

        try {
            String extractedText;
            try (InputStream inputStream = storageService.downloadFile(resume.getStorageKey())) {
                extractedText = textExtractionService.extract(inputStream, resume.getMimeType());
            }
            resume.saveExtractedText(extractedText, null);

            resume.markProcessing(LocalDateTime.now());
            resumeRepository.save(resume);

            markdownService.convert(resume);
            resumeRepository.save(resume);

            aiReviewService.generate(resume);
            resumeRepository.save(resume);

            aiScoringService.generateAndSave(resume);

            aiQuestionService.generateAndSave(resume);

            resume.markCompleted(LocalDateTime.now());
            resumeRepository.save(resume);

            notificationCreationService.notifySuccess(resume);
            log.info("Analysis completed for resumeId={}", resume.getId());

        } catch (TextExtractionException e) {
            log.warn("Text extraction failed for resumeId={}: {}", resume.getId(), e.getMessage());
            resume.markFailed(e.getMessage(), LocalDateTime.now());
            resumeRepository.save(resume);
            notificationCreationService.notifyFailure(resume);
        } catch (Exception e) {
            log.error("Analysis failed for resumeId={}", resume.getId(), e);
            resume.markFailed("분석 중 오류가 발생했습니다.", LocalDateTime.now());
            resumeRepository.save(resume);
            notificationCreationService.notifyFailure(resume);
        }
    }
}

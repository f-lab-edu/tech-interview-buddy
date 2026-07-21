package com.example.tech_interview_buddy.app.service.resume;

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

    private final ResumeAnalysisTransactionRunner transactionRunner;
    private final ResumeStorageService storageService;
    private final TextExtractionService textExtractionService;
    private final ResumeMarkdownService markdownService;
    private final ResumeAiReviewService aiReviewService;
    private final ResumeAiScoringService aiScoringService;
    private final ResumeAiQuestionService aiQuestionService;
    private final NotificationCreationService notificationCreationService;
    private final Executor taskExecutor;

    public void analyze(Long resumeId) {
        taskExecutor.execute(() -> runPipeline(resumeId));
    }

    private void runPipeline(Long resumeId) {
        log.info("Analysis started for resumeId={}", resumeId);

        try {
            ResumeSource source = transactionRunner.read(resumeId,
                    resume -> new ResumeSource(resume.getStorageKey(), resume.getMimeType()));

            String extractedText;
            try (InputStream inputStream = storageService.downloadFile(source.storageKey())) {
                extractedText = textExtractionService.extract(inputStream, source.mimeType());
            }
            transactionRunner.update(resumeId, resume -> {
                resume.saveExtractedText(extractedText, null);
                resume.markProcessing(LocalDateTime.now());
            });

            String markdown = markdownService.convert(extractedText);
            if (markdown != null) {
                transactionRunner.update(resumeId, resume -> resume.saveMarkdown(markdown));
            }

            ResumeAiReviewService.Result review = aiReviewService.generate(extractedText);
            transactionRunner.update(resumeId, resume -> resume.saveReviewResult(review.reviewJson(), review.summary()));

            transactionRunner.update(resumeId, aiScoringService::generateAndSave);

            transactionRunner.update(resumeId, aiQuestionService::generateAndSave);

            transactionRunner.update(resumeId, resume -> {
                resume.markCompleted(LocalDateTime.now());
                notificationCreationService.notifySuccess(resume);
            });
            log.info("Analysis completed for resumeId={}", resumeId);

        } catch (ResumeDeletedException e) {
            log.info("Resume deleted during analysis, aborting - resumeId={}", resumeId);
        } catch (TextExtractionException e) {
            log.warn("Text extraction failed for resumeId={}: {}", resumeId, e.getMessage());
            failQuietly(resumeId, e.getMessage());
        } catch (Exception e) {
            log.error("Analysis failed for resumeId={}", resumeId, e);
            failQuietly(resumeId, "분석 중 오류가 발생했습니다.");
        }
    }

    private void failQuietly(Long resumeId, String reason) {
        transactionRunner.updateIfPresent(resumeId, resume -> {
            resume.markFailed(reason, LocalDateTime.now());
            notificationCreationService.notifyFailure(resume);
        });
    }

    private record ResumeSource(String storageKey, String mimeType) {}
}

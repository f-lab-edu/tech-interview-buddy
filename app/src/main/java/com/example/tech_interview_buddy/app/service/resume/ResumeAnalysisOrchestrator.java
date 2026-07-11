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

    private final ResumeAnalysisTxOps tx;
    private final ResumeStorageService storageService;
    private final TextExtractionService textExtractionService;
    private final ResumeMarkdownService markdownService;
    private final ResumeAiReviewService aiReviewService;
    private final ResumeAiScoringService aiScoringService;
    private final ResumeAiQuestionService aiQuestionService;
    private final NotificationCreationService notificationCreationService;
    private final Executor taskExecutor;

    /**
     * 이력서 분석 파이프라인을 비동기로 실행한다.
     * 분석은 대부분 LLM 호출(I/O)이라 실제 동시 실행 상한은 전역 스레드풀 크기가 결정한다.
     */
    public void analyze(Long resumeId) {
        taskExecutor.execute(() -> runPipeline(resumeId));
    }

    /**
     * 각 DB 쓰기는 {@link ResumeAnalysisTxOps}를 통해 "id 재조회 → 없으면 중단"으로 수행된다.
     * 그래서 대기 중이거나 실행 중에 이력서가 삭제되면 유령 행을 만들지 않고 조용히 멈춘다.
     * LLM·S3 등 느린 계산은 트랜잭션 밖에서 수행해 DB 커넥션을 오래 점유하지 않는다.
     * (scoring·questions 서비스는 자체 {@code @Transactional}이라 재조회한 managed 엔티티를 넘겨 호출한다.)
     */
    private void runPipeline(Long resumeId) {
        log.info("Analysis started for resumeId={}", resumeId);

        try {
            ResumeSource source = tx.read(resumeId,
                    r -> new ResumeSource(r.getStorageKey(), r.getMimeType()));

            String extractedText;
            try (InputStream inputStream = storageService.downloadFile(source.storageKey())) {
                extractedText = textExtractionService.extract(inputStream, source.mimeType());
            }
            tx.update(resumeId, r -> {
                r.saveExtractedText(extractedText, null);
                r.markProcessing(LocalDateTime.now());
            });

            String markdown = markdownService.convert(extractedText);
            if (markdown != null) {
                tx.update(resumeId, r -> r.saveMarkdown(markdown));
            }

            ResumeAiReviewService.Result review = aiReviewService.generate(extractedText);
            tx.update(resumeId, r -> r.saveReviewResult(review.reviewJson(), review.summary()));

            tx.update(resumeId, aiScoringService::generateAndSave);

            tx.update(resumeId, aiQuestionService::generateAndSave);

            tx.update(resumeId, r -> {
                r.markCompleted(LocalDateTime.now());
                notificationCreationService.notifySuccess(r);
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
        boolean marked = tx.updateIfPresent(resumeId, r -> {
            r.markFailed(reason, LocalDateTime.now());
            notificationCreationService.notifyFailure(r);
        });
        if (!marked) {
            log.info("Resume already deleted, skip failure handling - resumeId={}", resumeId);
        }
    }

    private record ResumeSource(String storageKey, String mimeType) {}
}

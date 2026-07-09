package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.app.service.notification.NotificationCreationService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiQuestionService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiReviewService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiScoringService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeMarkdownService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
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
     * 사용자별 파이프라인 꼬리(tail)를 보관하는 캐시.
     * 마지막 접근 후 30분이 지난 항목은 자동 제거되어 메모리 누수를 방지한다.
     * (분석 하나는 몇 분 내에 끝나므로 실행 중인 항목이 만료될 위험은 사실상 없다)
     */
    private final Cache<Long, CompletableFuture<Void>> userPipelines = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    /**
     * 이력서 분석 파이프라인을 비동기로 실행한다.
     * 같은 사용자의 분석은 이전 분석의 꼬리에 체이닝되어 동시에 하나만 실행된다 (순서 보장).
     * 대기 중인 분석은 콜백으로만 등록되므로 스레드를 점유하지 않는다.
     */
    public void analyze(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));
        Long userId = resume.getUser().getId();

        // compute()로 "꼬리 읽기 → 이어붙이기 → 새 꼬리 저장"을 원자적으로 수행한다.
        userPipelines.asMap().compute(userId, (id, tail) -> {
            CompletableFuture<Void> previous =
                    (tail != null) ? tail : CompletableFuture.completedFuture(null);
            return previous
                    .thenRunAsync(() -> runPipeline(resume), taskExecutor)
                    // 꼬리가 예외로 완료되면 다음 체인이 실행되지 않으므로 항상 정상 완료로 마감한다.
                    .exceptionally(e -> {
                        log.error("Unexpected pipeline error for resumeId={}", resume.getId(), e);
                        return null;
                    });
        });
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

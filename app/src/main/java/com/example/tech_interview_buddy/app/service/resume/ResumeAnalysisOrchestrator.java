package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiQuestionService;
import com.example.tech_interview_buddy.domain.service.resume.ResumeAiReviewService;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisOrchestrator {

    private final ResumeRepository resumeRepository;
    private final ResumeStorageService storageService;
    private final TextExtractionService textExtractionService;
    private final ResumeAiReviewService aiReviewService;
    private final ResumeAiQuestionService aiQuestionService;

    private final ConcurrentHashMap<Long, Semaphore> userSemaphores = new ConcurrentHashMap<>();

    /**
     * 이력서 분석 파이프라인을 비동기로 실행한다.
     * 같은 사용자의 분석은 동시에 하나만 실행된다 (순서 보장).
     */
    @Async("taskExecutor")
    public void analyze(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));

        Semaphore semaphore = userSemaphores.computeIfAbsent(
                resume.getUser().getId(), id -> new Semaphore(1, true));

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Analysis interrupted for resumeId={}", resumeId);
            return;
        }

        try {
            runPipeline(resume);
        } finally {
            semaphore.release();
        }
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

            aiReviewService.generateAndSave(resume);

            aiQuestionService.generateAndSave(resume);

            resume.markCompleted(LocalDateTime.now());
            resumeRepository.save(resume);

            log.info("Analysis completed for resumeId={}", resume.getId());

        } catch (TextExtractionException e) {
            log.warn("Text extraction failed for resumeId={}: {}", resume.getId(), e.getMessage());
            resume.markFailed(e.getMessage(), LocalDateTime.now());
            resumeRepository.save(resume);
        } catch (Exception e) {
            log.error("Analysis failed for resumeId={}", resume.getId(), e);
            resume.markFailed("분석 중 오류가 발생했습니다.", LocalDateTime.now());
            resumeRepository.save(resume);
        }
    }
}

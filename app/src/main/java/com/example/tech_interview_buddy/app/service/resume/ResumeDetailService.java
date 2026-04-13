package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.app.dto.response.resume.ResumeDetailResponse;
import com.example.tech_interview_buddy.app.dto.response.resume.ResumeReviewResponse;
import com.example.tech_interview_buddy.app.dto.response.resume.ResumeScoreResponse;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeQuestionRepository;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeScoreRepository;
import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import com.example.tech_interview_buddy.domain.resume.ResumeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeDetailService {

    private final ResumeRepository resumeRepository;
    private final ResumeQuestionRepository questionRepository;
    private final ResumeScoreRepository scoreRepository;
    private final ResumeStorageService storageService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ResumeDetailResponse getDetail(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "이력서에 접근할 수 없습니다."));

        String presignedUrl = null;
        if (resume.getMimeType() != null && resume.getMimeType().contains("pdf")) {
            presignedUrl = storageService.generatePresignedUrl(resume.getStorageKey());
        }

        String statusMessage = resolveStatusMessage(resume.getStatus());
        ResumeReviewResponse review = null;
        if (resume.getStatus() == ResumeStatus.DONE && resume.getReviewData() != null) {
            review = parseReview(resume.getReviewData(), resumeId);
        }

        ResumeScoreResponse scoring = null;
        if (resume.getStatus() == ResumeStatus.DONE) {
            scoring = buildScoringResponse(resumeId);
        }

        List<ResumeDetailResponse.QuestionItem> questions = questionRepository.findByResumeId(resumeId)
            .stream()
            .map(ResumeDetailResponse.QuestionItem::from)
            .collect(Collectors.toList());

        return ResumeDetailResponse.builder()
            .resumeId(resume.getId())
            .filename(resume.getOriginalFilename())
            .status(resume.getStatus().name())
            .fileSize(resume.getFileSize())
            .mimeType(resume.getMimeType())
            .uploadedAt(resume.getCreatedAt())
            .analysisCompletedAt(resume.getAnalysisCompletedAt())
            .presignedUrl(presignedUrl)
            .extractedText(resume.getExtractedText())
            .markdownText(resume.getSummarizedText())
            .statusMessage(statusMessage)
            .review(review)
            .scoring(scoring)
            .questions(questions)
            .build();
    }

    private String resolveStatusMessage(ResumeStatus status) {
        return switch (status) {
            case PROCESSING -> "분석 진행 중입니다.";
            case FAILED -> "분석에 실패했습니다. 이력서를 다시 업로드해 주세요.";
            default -> null;
        };
    }

    private ResumeScoreResponse buildScoringResponse(Long resumeId) {
        List<ResumeScore> scoreEntities = scoreRepository.findByResumeId(resumeId);
        if (scoreEntities.isEmpty()) {
            return null;
        }

        int totalScore = 0;
        int maxTotalScore = 0;
        List<ResumeScoreResponse.ScoreDetail> scoreDetails = new ArrayList<>();
        for (ResumeScore entity : scoreEntities) {
            totalScore += entity.getScore();
            maxTotalScore += entity.getMaxScore();
            scoreDetails.add(ResumeScoreResponse.ScoreDetail.from(entity));
        }

        return ResumeScoreResponse.builder()
                .totalScore(totalScore)
                .maxTotalScore(maxTotalScore)
                .scores(scoreDetails)
                .build();
    }

    private ResumeReviewResponse parseReview(String reviewData, Long resumeId) {
        try {
            return objectMapper.readValue(reviewData, ResumeReviewResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse review data for resumeId={}: {}", resumeId, e.getMessage());
            return null;
        }
    }
}

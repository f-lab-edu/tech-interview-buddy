package com.example.tech_interview_buddy.app.service.resume;

/**
 * 분석 파이프라인 진행 중 대상 이력서가 삭제되었음을 알리는 내부 제어 신호.
 * 오케스트레이터가 잡아 파이프라인을 조용히 중단하는 데 쓰인다.
 */
public class ResumeDeletedException extends RuntimeException {

    public ResumeDeletedException(Long resumeId) {
        super("Resume deleted during analysis: " + resumeId);
    }
}

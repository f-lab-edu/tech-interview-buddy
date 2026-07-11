package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분석 파이프라인의 각 DB 접근을 "id로 재조회 → (없으면 중단) → 처리" 형태로 감싼다.
 *
 * <p>재조회한 managed 엔티티를 dirty checking으로 저장하므로, detached 엔티티를 merge하다
 * 삭제된 행을 되살리는 유령 행 문제가 원천적으로 발생하지 않는다. 재조회와 쓰기가 같은
 * 트랜잭션 안에 있어 TOCTOU도 없다.
 *
 * <p>오케스트레이터와 분리된 별도 빈인 이유: 같은 클래스 내부 메서드 호출은 Spring의
 * 트랜잭션 프록시를 거치지 않아 {@code @Transactional}이 적용되지 않기 때문이다.
 */
@Component
@RequiredArgsConstructor
public class ResumeAnalysisTxOps {

    private final ResumeRepository resumeRepository;

    /** 재조회 후 값 하나를 읽는다. 이미 삭제됐으면 파이프라인을 중단시킨다. */
    @Transactional(readOnly = true)
    public <T> T read(Long resumeId, Function<Resume, T> reader) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeDeletedException(resumeId));
        return reader.apply(resume);
    }

    /** 재조회 후 변경을 적용한다(dirty checking으로 반영). 이미 삭제됐으면 파이프라인을 중단시킨다. */
    @Transactional
    public void update(Long resumeId, Consumer<Resume> mutation) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeDeletedException(resumeId));
        mutation.accept(resume);
    }

    /** 재조회 후 변경을 적용하되, 이미 삭제됐으면 조용히 건너뛴다(실패 처리용). */
    @Transactional
    public boolean updateIfPresent(Long resumeId, Consumer<Resume> mutation) {
        return resumeRepository.findById(resumeId)
                .map(resume -> {
                    mutation.accept(resume);
                    return true;
                })
                .orElse(false);
    }
}

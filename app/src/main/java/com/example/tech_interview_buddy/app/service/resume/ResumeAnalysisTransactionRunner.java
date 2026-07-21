package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class ResumeAnalysisTransactionRunner {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public <T> T read(Long resumeId, Function<Resume, T> reader) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeDeletedException(resumeId));
        return reader.apply(resume);
    }

    @Transactional
    public void update(Long resumeId, Consumer<Resume> mutation) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeDeletedException(resumeId));
        mutation.accept(resume);
    }

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

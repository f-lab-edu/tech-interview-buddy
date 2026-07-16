package com.example.tech_interview_buddy.domain.repository.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeScoreRepository extends JpaRepository<ResumeScore, Long> {

    List<ResumeScore> findByResumeId(Long resumeId);

    void deleteByResumeId(Long resumeId);
}

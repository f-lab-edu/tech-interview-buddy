package com.example.tech_interview_buddy.domain.repository.resume;

import com.example.tech_interview_buddy.domain.resume.ResumeQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeQuestionRepository extends JpaRepository<ResumeQuestion, Long>, ResumeQuestionRepositoryCustom {

    List<ResumeQuestion> findByResumeId(Long resumeId);
}

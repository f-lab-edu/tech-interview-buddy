package com.example.tech_interview_buddy.domain.repository.resume;

import com.example.tech_interview_buddy.domain.resume.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}

package com.example.tech_interview_buddy.domain.repository.resume;

import com.example.tech_interview_buddy.domain.resume.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    long countByUserId(Long userId);

    Page<Resume> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);
}

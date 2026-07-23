package com.example.tech_interview_buddy.domain.repository.recruit;

import com.example.tech_interview_buddy.domain.recruit.RecruiterRequest;
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterRequestRepository extends JpaRepository<RecruiterRequest, Long> {

    boolean existsByUserIdAndStatus(Long userId, RecruiterRequestStatus status);

    List<RecruiterRequest> findByStatusOrderByCreatedAtAsc(RecruiterRequestStatus status);
}

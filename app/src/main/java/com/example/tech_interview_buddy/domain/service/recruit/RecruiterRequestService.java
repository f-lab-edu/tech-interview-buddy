package com.example.tech_interview_buddy.domain.service.recruit;

import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.UserRole;
import com.example.tech_interview_buddy.domain.exception.DuplicateResourceException;
import com.example.tech_interview_buddy.domain.exception.ResourceNotFoundException;
import com.example.tech_interview_buddy.domain.recruit.Company;
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequest;
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequestStatus;
import com.example.tech_interview_buddy.domain.repository.UserRepository;
import com.example.tech_interview_buddy.domain.repository.recruit.CompanyRepository;
import com.example.tech_interview_buddy.domain.repository.recruit.RecruiterRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruiterRequestService {

    private final RecruiterRequestRepository recruiterRequestRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecruiterRequest apply(Long userId, String companyName, String contactInfo) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == UserRole.RECRUITER) {
            throw new DuplicateResourceException("이미 채용담당자로 승인된 계정입니다.");
        }
        if (recruiterRequestRepository.existsByUserIdAndStatus(userId, RecruiterRequestStatus.PENDING)) {
            throw new DuplicateResourceException("처리 대기 중인 신청이 이미 있습니다.");
        }

        RecruiterRequest request = RecruiterRequest.builder()
            .user(user)
            .companyName(companyName)
            .contactInfo(contactInfo)
            .build();

        return recruiterRequestRepository.save(request);
    }

    public List<RecruiterRequest> findByStatus(RecruiterRequestStatus status) {
        if (status == null) {
            return recruiterRequestRepository.findAllBy();
        }
        return recruiterRequestRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    /**
     * 신청 승인: 회사가 없으면 생성하고(같은 이름이면 기존 회사에 합류),
     * 신청자를 RECRUITER로 승격한다.
     */
    @Transactional
    public RecruiterRequest approve(Long requestId) {
        RecruiterRequest request = findRequest(requestId);

        Company company = companyRepository.findByName(request.getCompanyName())
            .orElseGet(() -> companyRepository.save(
                Company.builder().name(request.getCompanyName()).build()));

        request.approve(LocalDateTime.now());
        request.getUser().promoteToRecruiter(company);

        log.info("Recruiter request approved - requestId={}, userId={}, companyId={}",
            requestId, request.getUser().getId(), company.getId());
        return request;
    }

    @Transactional
    public RecruiterRequest reject(Long requestId) {
        RecruiterRequest request = findRequest(requestId);
        request.reject(LocalDateTime.now());

        log.info("Recruiter request rejected - requestId={}, userId={}",
            requestId, request.getUser().getId());
        return request;
    }

    private RecruiterRequest findRequest(Long requestId) {
        return recruiterRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterRequest not found with id: " + requestId));
    }
}

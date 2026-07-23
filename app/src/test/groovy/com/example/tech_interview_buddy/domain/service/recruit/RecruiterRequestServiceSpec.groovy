package com.example.tech_interview_buddy.domain.service.recruit

import com.example.tech_interview_buddy.domain.User
import com.example.tech_interview_buddy.domain.UserRole
import com.example.tech_interview_buddy.domain.exception.DuplicateResourceException
import com.example.tech_interview_buddy.domain.exception.InvalidStateException
import com.example.tech_interview_buddy.domain.recruit.Company
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequest
import com.example.tech_interview_buddy.domain.recruit.RecruiterRequestStatus
import com.example.tech_interview_buddy.domain.repository.UserRepository
import com.example.tech_interview_buddy.domain.repository.recruit.CompanyRepository
import com.example.tech_interview_buddy.domain.repository.recruit.RecruiterRequestRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class RecruiterRequestServiceSpec extends Specification {

    RecruiterRequestRepository recruiterRequestRepository = Mock()
    CompanyRepository companyRepository = Mock()
    UserRepository userRepository = Mock()

    @Subject
    RecruiterRequestService service = new RecruiterRequestService(
        recruiterRequestRepository, companyRepository, userRepository
    )

    private static User user(UserRole role = UserRole.USER) {
        return User.builder()
                .username("applicant")
                .password("pw")
                .email("a@foo.com")
                .role(role)
                .build()
    }

    private static RecruiterRequest pendingRequest(User user) {
        return RecruiterRequest.builder()
                .user(user)
                .companyName("테크컴퍼니")
                .contactInfo("hr@techcompany.com")
                .build()
    }


    def "이미 채용담당자로 승인된 사용자가 신청하면 DuplicateResourceException을 던진다"() {
        given:
        userRepository.findById(1L) >> Optional.of(user(UserRole.RECRUITER))

        when:
        service.apply(1L, "테크컴퍼니", "hr@techcompany.com")

        then:
        thrown(DuplicateResourceException)
        0 * recruiterRequestRepository.save(_)
    }

    def "이미 처리 대기 중인 신청이 있으면 DuplicateResourceException을 던진다"() {
        given:
        userRepository.findById(1L) >> Optional.of(user())
        recruiterRequestRepository.existsByUserIdAndStatus(1L, RecruiterRequestStatus.PENDING) >> true

        when:
        service.apply(1L, "테크컴퍼니", "hr@techcompany.com")

        then:
        thrown(DuplicateResourceException)
        0 * recruiterRequestRepository.save(_)
    }

    def "정상 신청이면 PENDING 상태로 저장한다"() {
        given:
        userRepository.findById(1L) >> Optional.of(user())
        recruiterRequestRepository.existsByUserIdAndStatus(1L, RecruiterRequestStatus.PENDING) >> false

        when:
        service.apply(1L, "테크컴퍼니", "hr@techcompany.com")

        then:
        1 * recruiterRequestRepository.save({ RecruiterRequest saved ->
            saved.companyName == "테크컴퍼니" &&
            saved.contactInfo == "hr@techcompany.com" &&
            saved.status == RecruiterRequestStatus.PENDING
        })
    }

    // === approve ===

    def "이미 처리된 신청을 다시 승인하면 InvalidStateException을 던진다"() {
        given:
        def request = pendingRequest(user())
        request.approve(LocalDateTime.now())   // 이미 처리됨(APPROVED) 상태로 만든다
        recruiterRequestRepository.findById(1L) >> Optional.of(request)
        companyRepository.findByName("테크컴퍼니") >> Optional.of(Company.builder().name("테크컴퍼니").build())

        when:
        service.approve(1L)

        then:
        thrown(InvalidStateException)
    }

    def "승인 시 같은 이름의 회사가 있으면 새로 만들지 않고 기존 회사에 연결한다"() {
        given:
        def applicant = user()
        def existingCompany = Company.builder().name("테크컴퍼니").description("기존 회사").build()
        recruiterRequestRepository.findById(1L) >> Optional.of(pendingRequest(applicant))
        companyRepository.findByName("테크컴퍼니") >> Optional.of(existingCompany)

        when:
        service.approve(1L)

        then:
        0 * companyRepository.save(_)                 // 기존 회사 재사용 → 새로 저장하지 않음
        applicant.role == UserRole.RECRUITER          // 신청자가 담당자로 승격
        applicant.company == existingCompany          // 기존 회사에 연결
    }

    def "승인 시 같은 이름의 회사가 없으면 새 회사를 생성한다"() {
        given:
        def applicant = user()
        def request = RecruiterRequest.builder()
                .user(applicant).companyName("뉴컴퍼니").contactInfo("hr@newcompany.com").build()
        recruiterRequestRepository.findById(1L) >> Optional.of(request)
        companyRepository.findByName("뉴컴퍼니") >> Optional.empty()

        when:
        service.approve(1L)

        then:
        1 * companyRepository.save({ Company c -> c.name == "뉴컴퍼니" }) >> { Company c -> c }   // 새 회사 생성, 저장된 회사 반환
        applicant.role == UserRole.RECRUITER
    }

    // === reject ===

    def "이미 처리된 신청을 반려하면 InvalidStateException을 던진다"() {
        given:
        def request = pendingRequest(user())
        request.reject(LocalDateTime.now())   // 이미 반려된 상태
        recruiterRequestRepository.findById(1L) >> Optional.of(request)

        when:
        service.reject(1L)

        then:
        thrown(InvalidStateException)
    }
}

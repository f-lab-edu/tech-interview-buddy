package com.example.tech_interview_buddy.domain.service

import com.example.tech_interview_buddy.common.security.PasswordEncoder
import com.example.tech_interview_buddy.domain.User
import com.example.tech_interview_buddy.domain.UserRole
import com.example.tech_interview_buddy.domain.exception.InvalidStateException
import com.example.tech_interview_buddy.domain.exception.ResourceNotFoundException
import com.example.tech_interview_buddy.domain.recruit.Company
import com.example.tech_interview_buddy.domain.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    PasswordEncoder passwordEncoder = Mock()

    @Subject
    UserService service = new UserService(userRepository, passwordEncoder)

    def "채용담당자 권한을 회수하면 일반 사용자로 되돌리고 회사 배정을 해제한다"() {
        given:
        def recruiter = User.builder()
                .username("applicant").password("pw").email("a@foo.com")
                .role(UserRole.RECRUITER)
                .build()
        recruiter.promoteToRecruiter(Company.builder().name("테크컴퍼니").build())   // 회사 배정된 상태
        userRepository.findById(1L) >> Optional.of(recruiter)

        when:
        def result = service.revokeRecruiter(1L)

        then:
        result.role == UserRole.USER
        result.company == null
    }

    def "채용담당자가 아닌 사용자를 회수하려 하면 InvalidStateException을 던진다"() {
        given:
        def normalUser = User.builder()
                .username("normal").password("pw").email("n@foo.com")
                .role(UserRole.USER)
                .build()
        userRepository.findById(1L) >> Optional.of(normalUser)

        when:
        service.revokeRecruiter(1L)

        then:
        thrown(InvalidStateException)
        normalUser.role == UserRole.USER   // 상태는 그대로
    }

    def "존재하지 않는 사용자를 회수하려 하면 ResourceNotFoundException을 던진다"() {
        given:
        userRepository.findById(1L) >> Optional.empty()

        when:
        service.revokeRecruiter(1L)

        then:
        thrown(ResourceNotFoundException)
    }
}

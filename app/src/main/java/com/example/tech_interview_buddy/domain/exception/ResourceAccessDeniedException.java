package com.example.tech_interview_buddy.domain.exception;

/**
 * 리소스에 대한 권한이 없을 때(예: 남의 답변 수정 시도) 던진다. HTTP 403으로 매핑된다.
 * Spring Security의 AccessDeniedException과 구분하기 위해 Resource 접두어를 사용한다.
 */
public class ResourceAccessDeniedException extends RuntimeException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}

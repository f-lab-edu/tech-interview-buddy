package com.example.tech_interview_buddy.domain.exception;


/**
 * 리소스의 현재 상태에서 허용되지 않는 작업을 시도할 때 던진다.
 * (예: 이미 처리된 신청, 마감된 공고에 지원 등). HTTP 409로 매핑된다.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
    
}

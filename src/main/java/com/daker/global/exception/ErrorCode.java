package com.daker.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 해커톤
    HACKATHON_NOT_FOUND(HttpStatus.NOT_FOUND, "해커톤을 찾을 수 없습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 참가 신청된 해커톤입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참가 신청 정보를 찾을 수 없습니다."),

    // 팀
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "팀장만 수행할 수 있습니다."),
    TEAM_FULL(HttpStatus.CONFLICT, "팀 정원이 가득 찼습니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 합류 신청한 팀입니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 정보를 찾을 수 없습니다."),

    // 제출
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "제출물을 찾을 수 없습니다."),
    CANNOT_RESUBMIT(HttpStatus.BAD_REQUEST, "재제출이 불가합니다.");

    private final HttpStatus status;
    private final String message;
}

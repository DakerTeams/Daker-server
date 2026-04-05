package com.daker.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "필수 필드 누락 또는 유효성 검증 실패"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류"),

    // 인증 (AUTH)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었거나 유효하지 않습니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 누락되었습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    GITHUB_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "GitHub 로그인 처리에 실패했습니다."),
    GITHUB_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "GitHub 계정 이메일을 확인할 수 없습니다."),
    GITHUB_STATE_INVALID(HttpStatus.BAD_REQUEST, "GitHub 로그인 인증 상태가 유효하지 않습니다."),

    // 유저 (USER)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_TAG(HttpStatus.CONFLICT, "이미 등록된 태그입니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    NOT_A_JUDGE(HttpStatus.BAD_REQUEST, "해당 사용자는 심사위원이 아닙니다."),
    ALREADY_A_JUDGE(HttpStatus.CONFLICT, "이미 심사위원입니다."),
    JUDGE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 해당 해커톤에 배정된 심사위원입니다."),
    JUDGE_NOT_ASSIGNED(HttpStatus.NOT_FOUND, "해당 해커톤에 배정된 심사위원이 아닙니다."),

    // 해커톤 (HACKATHON)
    HACKATHON_NOT_FOUND(HttpStatus.NOT_FOUND, "해커톤을 찾을 수 없습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 참가 신청한 해커톤입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참가 신청 정보를 찾을 수 없습니다."),
    REGISTRATION_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "신청 기간이 아닙니다."),
    SUBMISSION_DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST, "제출 마감 시간이 초과되었습니다."),
    LEADERBOARD_NOT_PUBLIC(HttpStatus.FORBIDDEN, "리더보드는 최종 마감 이후 공개됩니다."),

    // 팀 (TEAM)
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "팀장만 수행할 수 있습니다."),
    TEAM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 해커톤에 팀이 있습니다."),
    TEAM_FULL(HttpStatus.CONFLICT, "팀 정원이 가득 찼습니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 합류 신청한 팀입니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 정보를 찾을 수 없습니다."),
    TEAM_APPLICATION_CLOSED(HttpStatus.BAD_REQUEST, "마감 후 팀 신청/합류가 불가합니다."),

    // 심사 (SCORE)
    JUDGE_ONLY(HttpStatus.FORBIDDEN, "심사위원 권한이 필요합니다."),
    SCORE_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "해당 해커톤의 심사 방식과 맞지 않는 요청입니다."),

    // 투표 (VOTE)
    VOTE_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "투표 기간이 아닙니다. (제출 마감 ~ 해커톤 종료일)"),
    ALREADY_VOTED(HttpStatus.CONFLICT, "이미 투표하셨습니다."),
    CANNOT_VOTE_OWN_TEAM(HttpStatus.FORBIDDEN, "자신이 속한 팀에는 투표할 수 없습니다."),
    VOTE_RESULT_NOT_PUBLIC(HttpStatus.FORBIDDEN, "투표 결과는 해커톤 종료 후 공개됩니다."),

    // 채팅 (CHAT)
    ALREADY_JOINED_CHAT(HttpStatus.CONFLICT, "이미 참가한 채팅방입니다."),

    // 제출 (SUBMISSION)
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "제출물을 찾을 수 없습니다."),
    ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 제출한 팀입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 확장자입니다. (ZIP, PDF, URL만 허용)"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 초과되었습니다. (최대 50MB)");

    private final HttpStatus status;
    private final String message;
}

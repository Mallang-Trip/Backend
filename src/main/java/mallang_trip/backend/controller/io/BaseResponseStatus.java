package mallang_trip.backend.controller.io;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    Success(200, "요청에 성공하였습니다."),
    Bad_Request(400, "요청이 잘못되었습니다."),
    Unauthorized(401, "인증되지 않은 사용자입니다."),
    Forbidden(403, "잘못된 접근입니다."),
    Not_Found(404, "찾을 수 없습니다."),
    Conflict(409, "데이터 중복 발생."),

    FILE_CONVERT_ERROR(10000, "파일 변환에 실패하였습니다."),
    EMPTY_JWT(10001, "JWT가 비어있습니다."),
    INVALID_JWT(10002, "JWT에 오류가 있습니다."),
    EXPIRED_JWT(10003, "JWT가 만료되었습니다."),

    CANNOT_FOUND_USER(20000, "유저를 찾을 수 없습니다."),
    PARTY_NOT_RECRUITING(20001, "모집이 종료되었거나, 다른 신청자가 존재합니다."),
    EXCEED_PARTY_CAPACITY(20002, "모집 인원 초과입니다."),
    CANNOT_CHANGE_COURSE(20003, "코스 변경이 불가능한 상태입니다."),
    PROPOSAL_END(20004, "종료된 제안입니다."),
    ;

    private final int statusCode;
    private final String message;

    private BaseResponseStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
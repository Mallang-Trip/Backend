package mallang_trip.backend.domain.global.io;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    Success(200, "요청에 성공하였습니다."),
    Bad_Request(400, "요청이 잘못되었습니다."),
    Unauthorized(401, "인증되지 않은 사용자입니다."),
    Forbidden(403, "잘못된 접근입니다."),
    Not_Found(404, "찾을 수 없습니다."),
    Conflict(409, "데이터 중복 발생."),
    Internal_Server_Error(500, "알 수 없는 오류 발생."),

    FILE_CONVERT_ERROR(10000, "파일 변환에 실패하였습니다."),
    EMPTY_JWT(10001, "JWT가 비어있습니다."),
    INVALID_JWT(10002, "JWT에 오류가 있습니다."),
    EXPIRED_JWT(10003, "JWT가 만료되었습니다."),

    // User
    CANNOT_FOUND_USER(404, "유저를 찾을 수 없습니다."),
    CANNOT_FOUND_DRIVER(404, "드라이버를 찾을 수 없습니다."),
    SUSPENDING(403, "정지된 사용자입니다."),

    // Destination
    CANNOT_FOUND_DESTINATION(404, "여행지를 찾을 수 없습니다."),

    // Party
    CANNOT_FOUND_RESERVATION(404, "예약 정보를 찾을 수 없습니다."),
    CANNOT_FOUND_PAYMENT(404, "결제 정보를 찾을 수 없습니다."),
    CANNOT_FOUND_PARTY(404, "파티를 찾을 수 없습니다."),
    PARTY_CONFLICTED(409, "이미 예약된 파티가 있습니다."),
    NOT_PARTY_MEMBER(403, "파티원이 아닙니다."),
    ALREADY_PARTY_MEMBER(409, "이미 가입되어있는 파티입니다."),
    PARTY_NOT_RECRUITING(403, "모집이 종료되었거나, 다른 신청자가 존재합니다."),
    EXCEED_PARTY_CAPACITY(403, "모집 인원 초과입니다."),
    CANNOT_CHANGE_COURSE(403, "코스 변경이 불가능한 상태입니다."),
    EXPIRED_PROPOSAL(403, "종료된 제안입니다."),
    ONGOING_PARTY_EXISTS(403, "진행중인 여행이 있습니다."),

    // Chat
    CANNOT_FOUND_CHATROOM(404, "채팅방을 찾을 수 없습니다."),
    NOT_CHATROOM_MEMBER(403, "채팅방 멤버가 아닙니다."),
    CHATROOM_EXIT_FORBIDDEN(403, "파티 진행중에는 채팅방을 나갈 수 없습니다."),
    CANNOT_KICK_CHAT_MEMBER(403, "채팅방 추방 권한이 없습니다."),
    ;

    private final int statusCode;
    private final String message;

    private BaseResponseStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
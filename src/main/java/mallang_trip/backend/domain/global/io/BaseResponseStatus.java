package mallang_trip.backend.domain.global.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus implements ResponseStatus{

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
    ;

    private final int statusCode;
    private final String message;

}
package mallang_trip.backend.domain.dreamSecurity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum IdentificationException implements ResponseStatus {

    TOKEN_TIMEOUT(401, "본인확인 결과인증 후 10분 경과"),
    SESSION_ERROR(401 ,"세션값 검증 실패"),
    ;

    private final int statusCode;
    private final String message;
}

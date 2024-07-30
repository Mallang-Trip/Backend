package mallang_trip.backend.domain.dreamSecurity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum IdentificationException implements ResponseStatus {

    TOKEN_TIMEOUT(401, "검증결과 토큰 생성 10분 경과 오류"),
    SESSION_ERROR(401 ,"세션값에 저장된 거래ID 비교 실패"),
    ;

    private final int statusCode;
    private final String message;
}

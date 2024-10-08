package mallang_trip.backend.domain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum UserExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_USER(404, "유저를 찾을 수 없습니다."),
	PAYMENT_FAILED_EXISTS(403, "미납금이 존재합니다.")
	;

	private final int statusCode;
	private final String message;
}


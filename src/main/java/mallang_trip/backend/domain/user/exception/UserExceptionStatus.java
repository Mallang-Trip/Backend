package mallang_trip.backend.domain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.domain.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum UserExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_USER(404, "유저를 찾을 수 없습니다."),
	;

	private final int statusCode;
	private final String message;
}


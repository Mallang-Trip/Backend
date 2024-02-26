package mallang_trip.backend.domain.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum AdminExceptionStatus implements ResponseStatus {
	SUSPENDING(403, "정지된 사용자입니다."),
	;

	private final int statusCode;
	private final String message;
}

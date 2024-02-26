package mallang_trip.backend.domain.driver.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.domain.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum DriverExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_DRIVER(404, "드라이버를 찾을 수 없습니다."),
	;

	private final int statusCode;
	private final String message;
}

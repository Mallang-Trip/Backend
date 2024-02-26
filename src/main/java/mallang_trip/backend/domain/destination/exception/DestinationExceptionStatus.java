package mallang_trip.backend.domain.destination.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.domain.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum DestinationExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_DESTINATION(404, "여행지를 찾을 수 없습니다."),
	;

	private final int statusCode;
	private final String message;
}

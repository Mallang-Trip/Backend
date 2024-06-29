package mallang_trip.backend.domain.region.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum RegionException implements ResponseStatus {

	REGION_ALREADY_EXISTS(409, "이미 존재하는 지역입니다."),
	REGION_NOT_FOUND(404, "지역을 찾을 수 없습니다."),
	REGION_NOT_EMPTY(409, "지역에 드라이버가 존재합니다."),
	;

	private final int statusCode;
	private final String message;
}

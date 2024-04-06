package mallang_trip.backend.domain.payple.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum PaypleExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_CARD(404, "등록된 카드가 존재하지 않습니다."),
	BILLING_FAIL(403, "결제에 실패했습니다."),
	;

	private final int statusCode;
	private final String message;
}

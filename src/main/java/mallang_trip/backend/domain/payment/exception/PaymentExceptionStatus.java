package mallang_trip.backend.domain.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.domain.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum PaymentExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_PAYMENT(404, "결제 정보를 찾을 수 없습니다."),
	PAYMENT_FAIL(403, "결제에 실패했습니다."),
	;

	private final int statusCode;
	private final String message;
}

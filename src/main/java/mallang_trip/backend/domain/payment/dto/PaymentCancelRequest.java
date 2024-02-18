package mallang_trip.backend.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelRequest {

	private String cancelReason;
	private Integer cancelAmount;
}

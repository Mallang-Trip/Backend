package mallang_trip.backend.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequest {

    private Integer amount;
    private String customerKey;
    private String orderId;
    private String orderName;
}

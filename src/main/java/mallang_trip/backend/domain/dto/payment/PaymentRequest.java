package mallang_trip.backend.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequest {

    private String customerKey;
    private String methodKey;
    private Integer amount;
    private String orderId;
    private String orderName;
}

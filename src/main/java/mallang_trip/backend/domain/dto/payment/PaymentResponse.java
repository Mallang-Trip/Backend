package mallang_trip.backend.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentKey;
    private Receipt receipt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Receipt {
        private String url;
    }
}

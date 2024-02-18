package mallang_trip.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingKeyResponse {

    private String mId;
    private String customerKey;
    private String authenticatedAt;
    private String method;
    private String billingKey;
    private CardResponse card;
}
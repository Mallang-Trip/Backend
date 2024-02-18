package mallang_trip.backend.domains.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingKeyRequest {

    private String customerKey;
    private String authKey;
}

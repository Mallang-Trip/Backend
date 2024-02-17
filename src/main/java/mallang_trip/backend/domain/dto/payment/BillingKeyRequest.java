package mallang_trip.backend.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingKeyRequest {

    private String customerKey;
    private String authKey;
}

package mallang_trip.backend.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingKeyResponse {

    private String billingKey;
}

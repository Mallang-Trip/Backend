package mallang_trip.backend.domain.income.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommissionRateResponse {

    private String partyCommissionPercent;
    private String penaltyCommissionPercent;
}

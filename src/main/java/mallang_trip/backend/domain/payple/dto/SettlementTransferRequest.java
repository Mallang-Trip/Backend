package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementTransferRequest {
    private String cst_id;
    private String custKey;
    private String billing_tran_id;
    private String tran_amt;
}

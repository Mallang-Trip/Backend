package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementAccountRequest {
    private String cst_id;
    private String custKey;
    private String bank_code_std;
    private String account_num;
    private String account_holder_info_type;
    private String account_holder_info;
}

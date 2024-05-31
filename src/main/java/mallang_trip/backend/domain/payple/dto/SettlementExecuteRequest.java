package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementExecuteRequest {
    private String cst_id;
    private String custKey;
    private String group_key;
    private String billing_tran_id;
    private String execute_type;

    private String webhook_url; // test용일 때만
}

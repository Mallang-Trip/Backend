package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementExecuteResponse {
    private String result;
    private String message;
    private String cst_id;
    private String group_key;
    private String billing_tran_id;
    private String tot_tran_amt;
    private String remain_amt;
    private String execute_type;
    private String api_tran_dtm;
}

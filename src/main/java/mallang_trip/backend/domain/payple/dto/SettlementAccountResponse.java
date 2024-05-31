package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementAccountResponse {
    private String result;
    private String message;
    private String cst_id;
    private String sub_id;
    private String billing_tran_id;
    private String api_tran_dtm;
    private String bank_tran_id;
    private String bank_tran_date;
    private String bank_rsp_code;
    private String bank_code_std;
    private String bank_code_sub;
    private String bank_name;
    private String account_num;
    private String account_holder_name;
}

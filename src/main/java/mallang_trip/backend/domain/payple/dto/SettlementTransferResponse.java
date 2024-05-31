package mallang_trip.backend.domain.payple.dto;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementTransferResponse {

    private String result;
    private String message;
    private String cst_id;
    private String sub_id;
    private String distinct_key;
    private String group_key;
    private String billing_tran_id;
    private String tran_amt;
    private String remain_amt;
    private String print_content;
    private String api_tran_dtm;
}

package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelResponse {

	private String PCD_PAY_RST;
	private String PCD_PAY_CODE;
	private String PCD_PAY_MSG;
	private String PCD_PAY_OID;
	private String PCD_PAY_TYPE;
	private String PCD_PAYER_NO;
	private String PCD_PAYER_ID;
	private String PCD_PAY_GOODS;
	private String PCD_REGULER_FLAG;
	private String PCD_PAY_YEAR;
	private String PCD_PAY_MONTH;
	private String PCD_REFUND_TOTAL;
	private String PCD_REFUND_TAXTOTAL;
	private String PCD_PAY_TIME;
	private String PCD_PAY_CARDTRADENUM;
	private String PCD_PAY_CARDRECEIPT;

}

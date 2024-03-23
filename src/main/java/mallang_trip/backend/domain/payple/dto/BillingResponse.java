package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingResponse {

	private String PCD_PAY_RST;
	private String PCD_PAY_CODE;
	private String PCD_PAY_MSG;
	private String PCD_PAY_OID;
	private String PCD_PAY_TYPE;
	private String PCD_PAYER_ID;
	private String PCD_PAYER_NO;
	private String PCD_PAYER_NAME;
	private String PCD_PAYER_HP;
	private String PCD_PAYER_EMAIL;
	private String PCD_PAY_GOODS;
	private String PCD_PAY_TOTAL;
	private String PCD_PAY_TAXTOTAL;
	private String PCD_PAY_ISTAX;
	private String PCD_PAY_TIME;
	private String PCD_PAY_CARDNAME;
	private String PCD_PAY_CARDNUM;
	private String PCD_PAY_CARDTRADENUM;
	private String PCD_PAY_CARDAUTHNO;
	private String PCD_PAY_CARDRECEIPT;
	private String PCD_SIMPLE_FLAG;
	private String PCD_USER_DEFINE1;
	private String PCD_USER_DEFINE2;

}

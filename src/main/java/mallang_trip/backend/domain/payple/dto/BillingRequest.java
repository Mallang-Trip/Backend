package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingRequest {

	private String PCD_CST_ID;
	private String PCD_CUST_KEY;
	private String PCD_AUTH_KEY;
	private String PCD_PAY_TYPE;
	private String PCD_PAYER_ID;
	private String PCD_PAY_GOODS;
	private String PCD_SIMPLE_FLAG;
	private Integer PCD_PAY_TOTAL;
	private String PCD_PAY_OID;
	private Long PCD_PAYER_NO;
	private String PCD_PAYER_NAME;
	private String PCD_PAYER_HP;
	private String PCD_PAYER_EMAIL;
	private String PCD_PAY_ISTAX;
	private Integer PCD_PAY_TAXTOTAL;
	private String PCD_USER_DEFINE1;
	private String PCD_USER_DEFINE2;
}

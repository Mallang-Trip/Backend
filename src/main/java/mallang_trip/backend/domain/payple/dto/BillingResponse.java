package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingResponse {

	@JsonProperty("PCD_PAY_RST")
	private String pcd_PAY_RST;

	@JsonProperty("PCD_PAY_CODE")
	private String pcd_PAY_CODE;

	@JsonProperty("PCD_PAY_MSG")
	private String pcd_PAY_MSG;

	@JsonProperty("PCD_PAY_OID")
	private String pcd_PAY_OID;

	@JsonProperty("PCD_PAY_TYPE")
	private String pcd_PAY_TYPE;

	@JsonProperty("PCD_PAYER_ID")
	private String pcd_PAYER_ID;

	@JsonProperty("PCD_PAYER_NO")
	private String pcd_PAYER_NO;

	@JsonProperty("PCD_PAYER_NAME")
	private String pcd_PAYER_NAME;

	@JsonProperty("PCD_PAYER_HP")
	private String pcd_PAYER_HP;

	@JsonProperty("PCD_PAYER_EMAIL")
	private String pcd_PAYER_EMAIL;

	@JsonProperty("PCD_PAY_GOODS")
	private String pcd_PAY_GOODS;

	@JsonProperty("PCD_PAY_TOTAL")
	private String pcd_PAY_TOTAL;

	@JsonProperty("PCD_PAY_TAXTOTAL")
	private String pcd_PAY_TAXTOTAL;

	@JsonProperty("PCD_PAY_ISTAX")
	private String pcd_PAY_ISTAX;

	@JsonProperty("PCD_PAY_TIME")
	private String pcd_PAY_TIME;

	@JsonProperty("PCD_PAY_CARDNAME")
	private String pcd_PAY_CARDNAME;

	@JsonProperty("PCD_PAY_CARDNUM")
	private String pcd_PAY_CARDNUM;

	@JsonProperty("PCD_PAY_CARDTRADENUM")
	private String pcd_PAY_CARDTRADENUM;

	@JsonProperty("PCD_PAY_CARDAUTHNO")
	private String pcd_PAY_CARDAUTHNO;

	@JsonProperty("PCD_PAY_CARDRECEIPT")
	private String pcd_PAY_CARDRECEIPT;

	@JsonProperty("PCD_SIMPLE_FLAG")
	private String pcd_SIMPLE_FLAG;

	@JsonProperty("PCD_USER_DEFINE1")
	private String pcd_USER_DEFINE1;

	@JsonProperty("PCD_USER_DEFINE2")
	private String pcd_USER_DEFINE2;
}

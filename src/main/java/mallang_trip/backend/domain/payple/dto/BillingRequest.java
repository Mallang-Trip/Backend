package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingRequest {

	@JsonProperty("PCD_CST_ID")
	private String pcd_CST_ID;

	@JsonProperty("PCD_CUST_KEY")
	private String pcd_CUST_KEY;

	@JsonProperty("PCD_AUTH_KEY")
	private String pcd_AUTH_KEY;

	@JsonProperty("PCD_PAY_TYPE")
	private String pcd_PAY_TYPE;

	@JsonProperty("PCD_PAYER_ID")
	private String pcd_PAYER_ID;

	@JsonProperty("PCD_PAY_GOODS")
	private String pcd_PAY_GOODS;

	@JsonProperty("PCD_SIMPLE_FLAG")
	private String pcd_SIMPLE_FLAG;

	@JsonProperty("PCD_PAY_TOTAL")
	private Integer pcd_PAY_TOTAL;

	@JsonProperty("PCD_PAY_OID")
	private String pcd_PAY_OID;

	@JsonProperty("PCD_PAYER_NO")
	private Long pcd_PAYER_NO;

	@JsonProperty("PCD_PAYER_NAME")
	private String pcd_PAYER_NAME;

	@JsonProperty("PCD_PAYER_HP")
	private String pcd_PAYER_HP;

	@JsonProperty("PCD_PAYER_EMAIL")
	private String pcd_PAYER_EMAIL;

	@JsonProperty("PCD_PAY_ISTAX")
	private String pcd_PAY_ISTAX;

	@JsonProperty("PCD_PAY_TAXTOTAL")
	private Integer pcd_PAY_TAXTOTAL;

	@JsonProperty("PCD_USER_DEFINE1")
	private String pcd_USER_DEFINE1;

	@JsonProperty("PCD_USER_DEFINE2")
	private String pcd_USER_DEFINE2;
}

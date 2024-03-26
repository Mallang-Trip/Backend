package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelResponse {

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

	@JsonProperty("PCD_PAYER_NO")
	private String pcd_PAYER_NO;

	@JsonProperty("PCD_PAYER_ID")
	private String pcd_PAYER_ID;

	@JsonProperty("PCD_PAY_GOODS")
	private String pcd_PAY_GOODS;

	@JsonProperty("PCD_REGULER_FLAG")
	private String pcd_REGULER_FLAG;

	@JsonProperty("PCD_PAY_YEAR")
	private String pcd_PAY_YEAR;

	@JsonProperty("PCD_PAY_MONTH")
	private String pcd_PAY_MONTH;

	@JsonProperty("PCD_REFUND_TOTAL")
	private String pcd_REFUND_TOTAL;

	@JsonProperty("PCD_REFUND_TAXTOTAL")
	private String pcd_REFUND_TAXTOTAL;

	@JsonProperty("PCD_PAY_TIME")
	private String pcd_PAY_TIME;

	@JsonProperty("PCD_PAY_CARDTRADENUM")
	private String pcd_PAY_CARDTRADENUM;

	@JsonProperty("PCD_PAY_CARDRECEIPT")
	private String pcd_PAY_CARDRECEIPT;

}

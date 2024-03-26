package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelRequest {

	@JsonProperty("PCD_CST_ID")
	private String pcd_CST_ID;

	@JsonProperty("PCD_CUST_KEY")
	private String pcd_CUST_KEY;

	@JsonProperty("PCD_AUTH_KEY")
	private String pcd_AUTH_KEY;

	@JsonProperty("PCD_REFUND_KEY")
	private String pcd_REFUND_KEY;

	@JsonProperty("PCD_PAYCANCEL_FLAG")
	private String pcd_PAYCANCEL_FLAG;

	@JsonProperty("PCD_PAY_OID")
	private String pcd_PAY_OID;

	@JsonProperty("PCD_PAY_DATE")
	private String pcd_PAY_DATE;

	@JsonProperty("PCD_REFUND_TOTAL")
	private String pcd_REFUND_TOTAL;

	@JsonProperty("PCD_REFUND_TAXTOTAL")
	private String pcd_REFUND_TAXTOTAL;

}

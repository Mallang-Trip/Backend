package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelRequest {

	private String PCD_CST_ID;
	private String PCD_CUST_KEY;
	private String PCD_AUTH_KEY;
	private String PCD_REFUND_KEY;
	private String PCD_PAYCANCEL_FLAG;
	private String PCD_PAY_OID;
	private String PCD_PAY_DATE;
	private String PCD_REFUND_TOTAL;
	private String PCD_REFUND_TAXTOTAL;

}

package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartnerAuthRequest {

	private String cst_id;
	private String custKey;
	private String PCD_PAY_TYPE;
	private String PCD_SIMPLE_FLAG;
	private String PCD_PAYCANCEL_FLAG;
}

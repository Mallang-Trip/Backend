package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartnerAuthRequest {

	private String cst_id;
	private String custKey;

	@JsonProperty("PCD_PAY_TYPE")
	private String pcd_PAY_TYPE;

	@JsonProperty("PCD_SIMPLE_FLAG")
	private String pcd_SIMPLE_FLAG;

	@JsonProperty("PCD_PAYCANCEL_FLAG")
	private String pcd_PAYCANCEL_FLAG;
}

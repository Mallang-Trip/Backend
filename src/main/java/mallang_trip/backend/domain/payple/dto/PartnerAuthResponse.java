package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartnerAuthResponse {

	private String server_name;
	private String result;
	private String result_msg;
	private String cst_id;
	private String custKey;
	private String return_url;

	@JsonProperty("AuthKey")
	private String authKey;

	@JsonProperty("PCD_PAY_HOST")
	private String pcd_PAY_HOST;

	@JsonProperty("PCD_PAY_URL")
	private String pcd_PAY_URL;

}

package mallang_trip.backend.domain.payple.dto;

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
	private String AuthKey;
	private String PCD_PAY_HOST;
	private String PCD_PAY_URL;
	private String return_url;

}

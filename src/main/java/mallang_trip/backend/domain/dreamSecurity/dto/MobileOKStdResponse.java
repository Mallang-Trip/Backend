package mallang_trip.backend.domain.dreamSecurity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MobileOKStdResponse {

	private String usageCode;
	private String serviceId;
	private String encryptReqClientInfo;
	private String serviceType;
	private String retTransferType;
	private String returnUrl;
}

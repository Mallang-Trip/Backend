package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCountResponse {

	private Long userCount;
	private Long driverCount;
}

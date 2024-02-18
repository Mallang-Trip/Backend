package mallang_trip.backend.domains.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuspensionRequest {

	private String content;
	private int duration;
}

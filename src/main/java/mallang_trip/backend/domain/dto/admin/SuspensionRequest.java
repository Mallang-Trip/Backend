package mallang_trip.backend.domain.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuspensionRequest {

	private String content;
	private int duration;
}

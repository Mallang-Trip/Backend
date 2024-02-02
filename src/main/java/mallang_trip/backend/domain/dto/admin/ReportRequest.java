package mallang_trip.backend.domain.dto.admin;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ReportType;

@Getter
@Builder
public class ReportRequest {

	private Long reporteeId;
	private String content;
	private ReportType type;
	private Long targetId;
}

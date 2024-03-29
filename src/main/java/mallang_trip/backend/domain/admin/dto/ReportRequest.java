package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportType;

@Getter
@Builder
public class ReportRequest {

	private Long reporteeId;
	private String content;
	private ReportType type;
	private Long targetId;
}

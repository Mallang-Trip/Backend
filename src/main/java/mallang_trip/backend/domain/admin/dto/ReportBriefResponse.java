package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.entity.Report;

@Getter
@Builder
public class ReportBriefResponse {

	private Long reportId;
	private String reporteeNickname;
	private Long reporteeId;
	private String reporteeLoginId;
	private ReportStatus status;
	private LocalDateTime createdAt;

	public static ReportBriefResponse of(Report report){
		return ReportBriefResponse.builder()
			.reportId(report.getId())
			.reporteeNickname(report.getReportee().getNickname())
			.reporteeId(report.getReportee().getId())
			.reporteeLoginId(report.getReportee().getLoginId())
			.status(report.getStatus())
			.createdAt(report.getCreatedAt())
			.build();
	}
}

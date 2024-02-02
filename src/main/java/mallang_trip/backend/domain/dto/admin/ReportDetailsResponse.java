package mallang_trip.backend.domain.dto.admin;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ReportStatus;
import mallang_trip.backend.constant.ReportType;
import mallang_trip.backend.domain.entity.admin.Report;

@Getter
@Builder
public class ReportDetailsResponse {

	private Long reportId;
	private Long targetId;
	private Long reporterId;
	private Long reporteeId;
	private String reporterNickname;
	private String reporteeNickname;
	private String content;
	private ReportStatus status;
	private ReportType type;
	private LocalDateTime createdAt;

	public static ReportDetailsResponse of(Report report){
		return ReportDetailsResponse.builder()
			.reportId(report.getId())
			.targetId(report.getTargetId())
			.reporterId(report.getReporter().getId())
			.reporteeId(report.getReportee().getId())
			.reporterNickname(report.getReporter().getNickname())
			.reporteeNickname(report.getReportee().getNickname())
			.content(report.getContent())
			.status(report.getStatus())
			.type(report.getType())
			.createdAt(report.getCreatedAt())
			.build();
	}
}

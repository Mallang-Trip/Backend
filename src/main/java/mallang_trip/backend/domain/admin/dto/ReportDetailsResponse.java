package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.constant.ReportType;
import mallang_trip.backend.domain.admin.entity.Report;

@Getter
@Builder
public class ReportDetailsResponse {

	private Long reportId;
	private Long targetId;
	private Long reporterId;
	private Long reporteeId;
	private String reporterNickname;
	private String reporteeNickname;
	private String reporterLoginId;
	private String reporteeLoginId;
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
			.reporterLoginId(report.getReporter().getLoginId())
			.reporteeLoginId(report.getReportee().getLoginId())
			.content(report.getContent())
			.status(report.getStatus())
			.type(report.getType())
			.createdAt(report.getCreatedAt())
			.build();
	}
}

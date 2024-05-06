package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.constant.ReportType;
import mallang_trip.backend.domain.admin.entity.Report;
import mallang_trip.backend.domain.admin.entity.Suspension;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportCompleteDetailsResponse {

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

    private Boolean suspensionExist;
    private String suspensionContent;

    // Suspension이 null이면 suspensionExist를 false로 설정 및 suspensionContent를 null로 설정
    public static ReportCompleteDetailsResponse of(Report report, Suspension suspension){
        return ReportCompleteDetailsResponse.builder()
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
                .suspensionExist(suspension != null)
                .suspensionContent(suspension != null ? suspension.getContent() : null)
                .build();
    }
}

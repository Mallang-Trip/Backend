package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.entity.Report;
import mallang_trip.backend.domain.admin.entity.Suspension;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportCompleteBriefResponse {

    private Long reportId;
    private String suspensionContent;
    private Long reporteeId;
    private String reporteeNickname;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private boolean suspensionExist;


    // Suspension이 null이면 suspensionExist를 false로 설정 및 suspensionContent를 null로 설정
    public static ReportCompleteBriefResponse of(Report report, Suspension suspension){
            return ReportCompleteBriefResponse.builder()
                    .reportId(report.getId())
                    .reporteeId(report.getReportee().getId())
                    .reporteeNickname(report.getReportee().getNickname())
                    .status(report.getStatus())
                    .createdAt(report.getCreatedAt())
                    .suspensionExist(suspension != null)
                    .suspensionContent(suspension != null ? suspension.getContent() : null)
                    .build();
    }
}

package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.entity.Report;

@Getter
@Builder
public class ObjectionBriefResponse {

    private Long reportId;
    private String reporteeNickname;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public static ReportBriefResponse of(Report report){
        return ReportBriefResponse.builder()
                .reportId(report.getId())
                .reporteeNickname(report.getReportee().getNickname())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

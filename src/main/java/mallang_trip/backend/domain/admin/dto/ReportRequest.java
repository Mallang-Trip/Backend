package mallang_trip.backend.domain.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ReportType;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class ReportRequest {

	@NotBlank
	@ApiModelProperty(value = "reporteeId", required = true)
	private Long reporteeId;

	@NotBlank
	@ApiModelProperty(value = "신고 내용", required = true)
	private String content;

	@NotBlank
	@ApiModelProperty(value = "신고 타입", required = true)
	private ReportType type;

	@NotBlank
	@ApiModelProperty(value = "targetId", required = true)
	private Long targetId;
}

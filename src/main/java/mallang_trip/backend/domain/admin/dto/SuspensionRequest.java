package mallang_trip.backend.domain.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class SuspensionRequest {

	@NotBlank
	@ApiModelProperty(value = "Report Id", required = true)
	private Long reportId; // 신고에 따른 제재여부 확인을 위해 필요

	@NotBlank
	@ApiModelProperty(value = "제재 사유", required = true)
	private String content;

	@NotBlank
	@ApiModelProperty(value = "정지 기간", required = true)
	private int duration;
}

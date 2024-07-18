package mallang_trip.backend.domain.region.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegionRequest {

	@NotBlank
	@ApiModelProperty(value = "지역 이름", required = true)
	private String name;

	@NotBlank
	@ApiModelProperty(value = "지역 이미지 URL", required = true)
	private String image;

	@NotBlank
	@ApiModelProperty(value = "지역 소속 시/도", required = true)
	private String province;
}

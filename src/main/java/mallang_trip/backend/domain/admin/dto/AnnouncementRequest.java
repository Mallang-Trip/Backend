package mallang_trip.backend.domain.admin.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.AnnouncementType;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
public class AnnouncementRequest {

	@NotBlank
	@ApiModelProperty(value = "title", required = true)
	private String title;

	@NotBlank
	@ApiModelProperty(value = "content", required = true)
	private String content;

	@ApiModelProperty(value = "이미지 URL 배열", required = false)
	private List<String> images;

	@NotBlank
	@ApiModelProperty(value = "게시판 타입 (ANNOUNCEMENT | FAQ)", required = true)
	private AnnouncementType type;
}

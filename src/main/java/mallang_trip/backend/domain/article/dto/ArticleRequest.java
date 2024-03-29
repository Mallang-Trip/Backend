package mallang_trip.backend.domain.article.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleRequest {

	@NotBlank
	@ApiModelProperty(value = "게시판 타입 (all | FIND_PARTNER | FREE_BOARD | FEEDBACK 중 하나)", required = true)
	private String type;

	@NotBlank
	@ApiModelProperty(value = "제목", required = true)
	private String title;

	@NotBlank
	@ApiModelProperty(value = "내용", required = true)
	private String content;

	@ApiModelProperty(value = "파티 ID", required = false)
	private Long partyId;

	@ApiModelProperty(value = "이미지 URL 배열", required = false)
	private List<String> images;
}

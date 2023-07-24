package mallang_trip.backend.domain.dto.article;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;

@Data
@NoArgsConstructor
public class ArticleRequest {

	@NotNull
	private String type;

	@NotNull
	private String title;

	@NotNull
	private String content;

}

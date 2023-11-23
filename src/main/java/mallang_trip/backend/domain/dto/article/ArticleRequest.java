package mallang_trip.backend.domain.dto.article;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;

@Getter
@Builder
public class ArticleRequest {

	@NotNull
	private String type;

	@NotNull
	private String title;

	@NotNull
	private String content;

	private Long partyId;
	private List<String> images;
}

package mallang_trip.backend.domain.dto.article;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateArticleResponse {

	private Long articleId;
}

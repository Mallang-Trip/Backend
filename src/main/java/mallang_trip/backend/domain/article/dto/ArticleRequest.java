package mallang_trip.backend.domain.article.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

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

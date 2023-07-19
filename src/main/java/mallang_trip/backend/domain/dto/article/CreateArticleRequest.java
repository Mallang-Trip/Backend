package mallang_trip.backend.domain.dto.article;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;

@Data
@NoArgsConstructor
public class CreateArticleRequest {

	private String type;

	@NotNull
	private String title;

	@NotNull
	private String content;

	public ArticleType getArticleType(){
		if(type.equals("FREE_BOARDER")) return ArticleType.FREE_BOARDER;
		else if(type.equals("FIND_PARTNER")) return ArticleType.FIND_PARTNER;
		else throw new BaseException(BaseResponseStatus.Bad_Request);
	}
}

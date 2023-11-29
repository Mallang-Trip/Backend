package mallang_trip.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ArticleType {

	FIND_PARTNER,
	FREE_BOARD,
	FEEDBACK,
	;

	@JsonCreator
	public static ArticleType from(String str){
		return ArticleType.valueOf(str.toUpperCase());
	}
}

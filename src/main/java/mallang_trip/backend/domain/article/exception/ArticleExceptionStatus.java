package mallang_trip.backend.domain.article.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum ArticleExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_ARTICLE(404, "게시글을 찾을 수 없습니다."),
	CANNOT_FOUND_COMMENT(404, "댓글을 찾을 수 없습니다."),
	CANNOT_FOUND_REPLY(404, "대댓글을 찾을 수 없습니다."),
	DELETION_FORBIDDEN(403, "삭제 권한이 없습니다."),
	MODIFICATION_FORBIDDEN(403, "수정 권한이 없습니다."),
	;

	private final int statusCode;
	private final String message;
}

package mallang_trip.backend.domain.chat.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum ChatExceptionStatus implements ResponseStatus {

	CANNOT_FOUND_CHATROOM(404, "채팅방을 찾을 수 없습니다."),
	NOT_CHATROOM_MEMBER(403, "채팅방 멤버가 아닙니다."),
	CHATROOM_EXIT_FORBIDDEN(403, "파티 진행중에는 채팅방을 나갈 수 없습니다."),
	CANNOT_KICK_CHAT_MEMBER(403, "채팅방 추방 권한이 없습니다."),
	;

	private final int statusCode;
	private final String message;
}

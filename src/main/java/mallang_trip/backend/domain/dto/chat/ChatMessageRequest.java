package mallang_trip.backend.domain.dto.chat;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ChatType;

@Getter
@Builder
public class ChatMessageRequest {

    private String accessToken;
    private Long chatRoomId;
    private ChatType type;
    private String content;
}

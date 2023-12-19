package mallang_trip.backend.domain.dto.chat;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ChatType;

@Getter
@Builder
public class ChatMessageRequest {

    private ChatType type;
    private Long chatRoomId;
    private Long userId;
    private String content;
}

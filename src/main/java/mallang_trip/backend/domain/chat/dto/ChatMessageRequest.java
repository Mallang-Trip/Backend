package mallang_trip.backend.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.chat.constant.ChatType;

@Getter
@Builder
public class ChatMessageRequest {

    private ChatType type;
    private String content;
}

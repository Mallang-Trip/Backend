package mallang_trip.backend.domain.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatReadRequest {

    private String accessToken;
    private Long chatRoomId;
}

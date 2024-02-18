package mallang_trip.backend.domains.chat.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.chat.constant.ChatRoomType;

@Getter
@Builder
public class ChatRoomBriefResponse {

    private Long chatRoomId;
    private ChatRoomType type;
    private String roomName;
    private String image;
    private String content;
    private Integer headCount;
    private Integer unreadCount;
    private LocalDateTime updatedAt;

}

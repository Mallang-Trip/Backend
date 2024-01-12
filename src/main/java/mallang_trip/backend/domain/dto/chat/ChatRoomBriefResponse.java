package mallang_trip.backend.domain.dto.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ChatRoomType;

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

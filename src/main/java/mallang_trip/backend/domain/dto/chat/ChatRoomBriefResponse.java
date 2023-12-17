package mallang_trip.backend.domain.dto.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomBriefResponse {

    private Long chatRoomId;
    private Boolean isGroup;
    private String roomName;
    private List<String> profileImages;
    private String content;
    private Integer headCount;
    private Integer unreadCount;
    private LocalDateTime updatedAt;

}

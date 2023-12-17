package mallang_trip.backend.domain.dto.chat;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.User.UserBriefResponse;

@Getter
@Builder
public class ChatRoomDetailsResponse {

    private Long chatRoomId;
    private Boolean isGroup;
    private Integer headCount;
    private String roomName;
    private List<UserBriefResponse> members;
    private List<ChatMessageResponse> messages;

}

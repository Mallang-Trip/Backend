package mallang_trip.backend.domain.chat.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.chat.constant.ChatRoomType;
import mallang_trip.backend.domain.user.dto.UserBriefResponse;

@Getter
@Builder
public class ChatRoomDetailsResponse {

    private Long chatRoomId;
    private ChatRoomType type;
    private Long partyId;
    private Boolean myParty;
    private Long publicRoomId;
    private Integer headCount;
    private String roomName;
    private List<ChatMemberResponse> members;
    private List<ChatMessageResponse> messages;
    private Boolean isBlock;
    private Boolean isBlocked;

}

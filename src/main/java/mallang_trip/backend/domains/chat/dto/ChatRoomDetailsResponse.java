package mallang_trip.backend.domains.chat.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.chat.constant.ChatRoomType;
import mallang_trip.backend.domains.user.dto.UserBriefResponse;

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
    private List<UserBriefResponse> members;
    private List<ChatMessageResponse> messages;

}

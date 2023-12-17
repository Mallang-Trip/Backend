package mallang_trip.backend.domain.dto.chat;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ChatType;
import mallang_trip.backend.domain.entity.chat.ChatMessage;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class ChatMessageResponse {

    private Long messageId;
    private ChatType type;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse of(ChatMessage message){
        User user = message.getUser();
        return ChatMessageResponse.builder()
            .messageId(message.getId())
            .type(message.getType())
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

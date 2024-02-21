package mallang_trip.backend.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.chat.constant.ChatType;
import mallang_trip.backend.domain.chat.entity.ChatMessage;
import mallang_trip.backend.domain.user.entity.User;

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
            .userId(user == null? null : user.getId())
            .nickname(user == null? null : user.getNickname())
            .profileImg(user == null? null : user.getProfileImage())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

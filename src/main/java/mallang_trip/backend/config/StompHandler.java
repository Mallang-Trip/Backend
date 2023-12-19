package mallang_trip.backend.config;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.entity.chat.ChatMember;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.chat.ChatMemberRepository;
import mallang_trip.backend.repository.chat.ChatRoomRepository;
import mallang_trip.backend.repository.redis.ChatRoomConnection;
import mallang_trip.backend.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomConnection chatRoomConnection;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            Long chatRoomId = Long.parseLong(accessor.getFirstNativeHeader("room-id"));
            connectToChatRoom(accessor, chatRoomId);
        }
        return message;
    }

    private void connectToChatRoom(StompHeaderAccessor accessor, Long chatRoomId) {
        User user = userService.getCurrentUser(accessor);
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 모두 읽음 표시
        member.setUnreadCount(0);
        // Redis 채팅 연결 상태 저장
        chatRoomConnection.saveConnection(member, room);
    }
}

package mallang_trip.backend.config;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.service.ChatService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            Long chatRoomId = Long.parseLong(accessor.getFirstNativeHeader("room-id"));
            // Redis user 정보 저장
            chatService.connectToChatRoom(accessor, chatRoomId);
        }
        return message;
    }
}

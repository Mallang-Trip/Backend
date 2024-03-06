package mallang_trip.backend.global.config.stomp;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.config.security.TokenProvider;
import mallang_trip.backend.domain.chat.entity.ChatRoom;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.chat.repository.ChatMemberRepository;
import mallang_trip.backend.domain.chat.repository.ChatRoomRepository;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompPreHandler implements ChannelInterceptor {

    private final CurrentUserService currentUserService;
    private final TokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        handleMessage(accessor);
        return message;
    }

    private void handleMessage(StompHeaderAccessor accessor) {
        switch (accessor.getCommand()) {
            case CONNECT:
                // JWT 유효성 검사
                tokenProvider.validateToken(extractAccessToken(accessor));
                break;
            case SUBSCRIBE:
                // SUBSCRIBE 권한 검사
                checkSubscribe(accessor);
                break;
            case SEND:
                // SEND 권한 검사
                checkSend(accessor);
                break;
        }
    }

    /**
     * StompHeaderAccessor 에서 헤더의 JWT 토큰 추출
     */
    private String extractAccessToken(StompHeaderAccessor accessor){
        String tokenHeader = accessor.getFirstNativeHeader("access-token");
        if (tokenHeader == null || tokenHeader.isEmpty()) {
            throw new MessageDeliveryException("EMPTY_JWT");
        }
        return tokenHeader.substring(7);
    }

    /**
     * 현재 유저가 해당 경로를 SUBSCRIBE 할 수 있는 권한이 있는지 확인
     */
    private void checkSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        User user = currentUserService.getCurrentUser(accessor);
        if (destination.startsWith("/sub/room")) {
            checkPermissionForChatRoom(user, destination);
        } else if (destination.startsWith("/sub/list")) {
            checkPermissionForChatList(user, destination);
        } else {
            throw new MessageDeliveryException("BAD_REQUEST");
        }
    }

    /**
     * 현재 유저가 해당 경로로 SEND 할 수 있는 권한이 있는지 확인
     */
    private void checkSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        User user = currentUserService.getCurrentUser(accessor);
        try{
            if (destination.startsWith("/pub/read") || destination.startsWith("/pub/write")) {
                Long roomId = Long.parseLong(accessor.getFirstNativeHeader("room-id"));
                ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new MessageDeliveryException("CANNOT_FOUND_CHAT_ROOM"));
                if (!chatMemberRepository.existsByChatRoomAndUser(room, user)) {
                    throw new MessageDeliveryException("UNAUTHORIZED");
                }
            } else {
                throw new MessageDeliveryException("BAD_REQUEST");
            }
        } catch (NumberFormatException e){
            throw new MessageDeliveryException("INVALID_FORMAT");
        }
    }

    private void checkPermissionForChatRoom(User user, String destination) {
        try {
            Long chatRoomId = Long.parseLong(destination.substring(10));
            ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new MessageDeliveryException("CANNOT_FOUND_CHAT_ROOM"));
            if (!chatMemberRepository.existsByChatRoomAndUser(room, user)) {
                throw new MessageDeliveryException("UNAUTHORIZED");
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new MessageDeliveryException("INVALID_FORMAT");
        }
    }

    private void checkPermissionForChatList(User user, String destination) {
        try {
            Long userId = Long.parseLong(destination.substring(10));
            if (user.getId() != userId) {
                throw new MessageDeliveryException("UNAUTHORIZED");
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new MessageDeliveryException("INVALID_FORMAT");
        }
    }
}

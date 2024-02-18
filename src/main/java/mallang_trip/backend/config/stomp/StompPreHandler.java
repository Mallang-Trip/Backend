package mallang_trip.backend.config.stomp;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.config.security.TokenProvider;
import mallang_trip.backend.domains.chat.entity.ChatRoom;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.chat.repository.ChatMemberRepository;
import mallang_trip.backend.domains.chat.repository.ChatRoomRepository;
import mallang_trip.backend.domains.user.repository.UserRepository;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompPreHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
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
                tokenProvider.validateToken(accessor);
                break;
            case SUBSCRIBE:
                checkSubscribe(accessor);
                break;
            case SEND:
                checkSend(accessor);
                break;
        }
    }

    private void checkSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        User user = getCurrentUser(accessor);
        if (destination.startsWith("/sub/room")) {
            checkPermissionForChatRoom(user, destination);
        } else if (destination.startsWith("/sub/list")) {
            checkPermissionForChatList(user, destination);
        } else {
            throw new MessageDeliveryException("BAD_REQUEST");
        }
    }

    private void checkSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        User user = getCurrentUser(accessor);
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

    private User getCurrentUser(StompHeaderAccessor accessor) {
        String accessToken = accessor.getFirstNativeHeader("access-token");
        if (accessToken == null) {
            throw new MessageDeliveryException("EMPTY_JWT");
        }
        String token = accessToken.substring(7);
        Authentication authentication = tokenProvider.getAuthentication(token);
        User user = userRepository.findById(Long.parseLong(authentication.getName()))
            .orElseThrow(() -> new MessageDeliveryException("CANNOT_FOUND_USER"));
        return user;
    }
}

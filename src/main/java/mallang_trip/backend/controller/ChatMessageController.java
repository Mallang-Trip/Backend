package mallang_trip.backend.controller;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.chat.ChatMessageRequest;
import mallang_trip.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    // SEND '/pub/write'
    @MessageMapping("/write")
    public void message(ChatMessageRequest message, StompHeaderAccessor accessor) throws Exception {
        template.convertAndSend("/sub/room/" + accessor.getFirstNativeHeader("room-id"),
            chatService.handleNewMessage(message, accessor));
    }

    // SEND '/pub/read'
    @MessageMapping("/read")
    public void read(StompHeaderAccessor accessor) {
        chatService.setUnreadCountZero(accessor);
    }
}

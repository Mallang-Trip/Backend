package mallang_trip.backend.controller;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.chat.ChatMessageRequest;
import mallang_trip.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/message")
    public void message(ChatMessageRequest message) throws Exception {
        template.convertAndSend("/sub/room/" + message.getChatRoomId(),
            chatService.handleNewMessage(message));
    }
}

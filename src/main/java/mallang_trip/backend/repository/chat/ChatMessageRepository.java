package mallang_trip.backend.repository.chat;

import java.util.List;
import mallang_trip.backend.domain.entity.chat.ChatMessage;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatroom);

    @Query(value = "SELECT cm.* FROM chat_message cm\n"
        + "    JOIN chat_room cr ON cm.chat_room_id = cr.id\n"
        + "    JOIN chat_member cmm ON cmm.chat_room_id = cr.id\n"
        + "WHERE cr.id = :chat_room_id\n"
        + "  AND cmm.user_id = :user_id\n"
        + "  AND cm.created_at > cmm.updated_at\n"
        + "ORDER BY cm.created_at ASC;", nativeQuery = true)
    List<ChatMessage> findByChatRoomAndUser(@Param(value = "chat_room_id") Long chatRoomId,
        @Param(value = "user_id") Long userId);
}

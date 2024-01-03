package mallang_trip.backend.repository.chat;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.constant.ChatType;
import mallang_trip.backend.domain.entity.chat.ChatMessage;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(value = "SELECT cm.* FROM chat_message cm\n"
        + "    JOIN chat_room cr ON cm.chat_room_id = cr.id\n"
        + "    JOIN chat_member cmm ON cmm.chat_room_id = cr.id\n"
        + "WHERE cr.id = :chat_room_id\n"
        + "  AND cmm.user_id = :user_id\n"
        + "  AND cm.created_at >= cmm.joined_at\n"
        + "ORDER BY cm.created_at ASC;", nativeQuery = true)
    List<ChatMessage> findByChatRoomAndUser(
        @Param(value = "chat_room_id") Long chatRoomId,
        @Param(value = "user_id") Long userId);


    @Query(value = "SELECT cm.* FROM chat_message cm " +
        "   JOIN chat_room cr ON cm.chat_room_id = cr.id " +
        "   JOIN chat_member cmm ON cmm.chat_room_id = cr.id " +
        "WHERE cr.id = :chat_room_id " +
        "   AND cmm.user_id = :user_id " +
        "   AND cm.created_at >= cmm.joined_at " +
        "   AND cm.type != 2 " +
        "ORDER BY cm.created_at DESC " +
        "LIMIT 1", nativeQuery = true)
    ChatMessage getLastMessage(
        @Param("chat_room_id") Long chatRoomId,
        @Param("user_id") Long userId
    );
}

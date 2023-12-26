package mallang_trip.backend.repository.chat;

import java.util.List;
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
    List<ChatMessage> findByChatRoomAndUser(@Param(value = "chat_room_id") Long chatRoomId,
        @Param(value = "user_id") Long userId);

    //ChatMessage findFirstByChatRoomAndTypeNotOrderByCreatedAtDesc(ChatRoom chatRoom, ChatType type);

    @Query("SELECT cm FROM ChatMessage cm " +
        "JOIN cm.chatRoom cr " +
        "JOIN ChatMember cmm ON cmm.chatRoom = cr " +
        "WHERE cr = :chatRoom " +
        "AND cmm.user = :user " +
        "AND cm.createdAt >= cmm.joinedAt " +
        "AND cm.type <> :type " +
        "ORDER BY cm.createdAt DESC")
    ChatMessage findFirstByChatRoomAndTypeNotOrderByCreatedAtDesc(
        @Param("chatRoom") ChatRoom chatRoom,
        @Param("user") User user,
        @Param("type") ChatType type
    );
}

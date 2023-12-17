package mallang_trip.backend.repository.chat;

import mallang_trip.backend.domain.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = "SELECT cr.* FROM chat_room cr\n"
        + "    JOIN chat_member cm1 ON cr.id = cm1.chat_room_id\n"
        + "    JOIN chat_member cm2 ON cr.id = cm2.chat_room_id\n"
        + "WHERE cr.is_group = false AND cm1.user_id = :user_id1 AND cm2.user_id = :user_id2;", nativeQuery = true)
    ChatRoom findExistedChatRoom(@Param(value = "user_id1") Long userId1, @Param(value = "user_id2")Long userId2);
}

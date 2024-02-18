package mallang_trip.backend.domain.chat.repository;

import java.util.Optional;
import mallang_trip.backend.domain.chat.constant.ChatRoomType;
import mallang_trip.backend.domain.chat.entity.ChatRoom;
import mallang_trip.backend.domain.party.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = "SELECT cr.* FROM chat_room cr\n"
        + "    JOIN chat_member cm1 ON cr.id = cm1.chat_room_id\n"
        + "    JOIN chat_member cm2 ON cr.id = cm2.chat_room_id\n"
        + "WHERE cr.type = 'COUPLE' AND cm1.user_id = :user_id_one AND cm2.user_id = :user_id_two", nativeQuery = true)
    ChatRoom findExistedChatRoom(@Param(value = "user_id_one") Long userId1, @Param(value = "user_id_two") Long userId2);

    Optional<ChatRoom> findByPartyAndType(Party party, ChatRoomType type);
}

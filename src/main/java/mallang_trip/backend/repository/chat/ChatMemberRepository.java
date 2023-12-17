package mallang_trip.backend.repository.chat;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.entity.chat.ChatMember;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    Boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    Integer countByChatRoom(ChatRoom chatRoom);

    List<ChatMember> findByUserAndActive(User user, boolean active);

    List<ChatMember> findByChatRoomAndActive(ChatRoom chatRoom, Boolean active);

    List<ChatMember> findByChatRoom(ChatRoom chatRoom);
}

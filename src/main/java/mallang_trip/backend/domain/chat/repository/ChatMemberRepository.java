package mallang_trip.backend.domain.chat.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.chat.entity.ChatMember;
import mallang_trip.backend.domain.chat.entity.ChatRoom;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    Boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    Integer countByChatRoom(ChatRoom chatRoom);

    List<ChatMember> findByUserAndActive(User user, boolean active);

    List<ChatMember> findByChatRoom(ChatRoom chatRoom);
}

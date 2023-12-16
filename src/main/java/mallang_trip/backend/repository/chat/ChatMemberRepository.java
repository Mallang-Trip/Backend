package mallang_trip.backend.repository.chat;

import mallang_trip.backend.domain.entity.chat.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

}

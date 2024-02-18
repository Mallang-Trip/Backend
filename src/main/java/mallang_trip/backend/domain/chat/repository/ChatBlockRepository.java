package mallang_trip.backend.domain.chat.repository;

import java.util.List;
import mallang_trip.backend.domain.chat.entity.ChatBlock;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatBlockRepository extends JpaRepository<ChatBlock, Long> {

	Boolean existsByUserAndTargetUser(User user, User targetUser);

	List<ChatBlock> findByUser(User user);

	void deleteByUserAndTargetUser(User user, User targetUser);
}

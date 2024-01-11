package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyDibs;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyDibsRepository extends JpaRepository<PartyDibs, Long> {

	Boolean existsByPartyAndUser(Party party, User user);

	List<PartyDibs> findAllByUserOrderByUpdatedAtDesc(User user);

	void deleteByPartyAndUser(Party party, User user);
}

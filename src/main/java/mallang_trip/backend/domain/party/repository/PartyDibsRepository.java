package mallang_trip.backend.domain.party.repository;

import java.util.List;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyDibs;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyDibsRepository extends JpaRepository<PartyDibs, Long> {

	Boolean existsByPartyAndUser(Party party, User user);

	List<PartyDibs> findAllByUserOrderByUpdatedAtDesc(User user);

	void deleteByPartyAndUser(Party party, User user);
}

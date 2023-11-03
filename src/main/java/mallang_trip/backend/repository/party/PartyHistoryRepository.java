package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyHistory;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyHistoryRepository extends JpaRepository<PartyHistory, Long> {

	List<PartyHistory> findByUserOrderByUpdatedAtDesc(User user);

	PartyHistory findByUserAndParty(User user, Party party);
 }

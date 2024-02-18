package mallang_trip.backend.domains.party.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domains.party.entity.Party;
import mallang_trip.backend.domains.party.entity.PartyHistory;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyHistoryRepository extends JpaRepository<PartyHistory, Long> {

	List<PartyHistory> findByUserOrderByUpdatedAtDesc(User user);

	Optional<PartyHistory> findByPartyAndUser(Party party, User user);
}

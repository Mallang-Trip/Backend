package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMembers;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyMembersRepository extends JpaRepository<PartyMembers, Long> {

    List<PartyMembers> findByParty(Party party);

    List<PartyMembers> findByUser(User user);

    Boolean existsByPartyAndUser(Party party, User user);
}

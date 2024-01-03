package mallang_trip.backend.repository.party;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    List<PartyMember> findByParty(Party party);

    List<PartyMember> findByUser(User user);

    Boolean existsByPartyAndUser(Party party, User user);

    Optional<PartyMember> findByPartyAndUser(Party party, User user);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'false' ELSE 'true' END\n"
        + "FROM party_member\n"
        + "WHERE party_id = :party_id AND ready = false", nativeQuery = true)
    Boolean isEveryoneReady(@Param(value = "party_id") Long partyId);
}

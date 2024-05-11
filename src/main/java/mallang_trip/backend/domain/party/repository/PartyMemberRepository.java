package mallang_trip.backend.domain.party.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.user.entity.User;
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
        + "WHERE party_id = :party_id AND ready = false AND deleted = 'false' ", nativeQuery = true)
    Boolean isEveryoneReady(@Param(value = "party_id") Long partyId);


    // find By User deleted = True
    @Query(value = "SELECT * FROM party_member\n"
        + "WHERE user_id = :user_id AND deleted = 'true'", nativeQuery = true)
    List<PartyMember> findByUserAndDeleted(@Param(value = "user_id") Long userId);
}

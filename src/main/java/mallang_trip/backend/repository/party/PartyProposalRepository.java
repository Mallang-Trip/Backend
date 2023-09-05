package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalRepository extends JpaRepository<PartyProposal, Long> {

    @Query(value = "SELECT CASE WHEN p.agreement = true\n"
        + "    AND NOT EXISTS (SELECT pa FROM party_agreement a WHERE a.proposal_id = p.id AND (a.agreement = 'REFUSE' OR a.agreement = 'WAITING'))\n"
        + "    THEN true ELSE false END\n"
        + "    FROM party_proposal p WHERE p.id = :proposalId", nativeQuery = true)
    Boolean isUnanimity(@Param(value = "proposalId") Long proposalId);

    @Query(value = "SELECT * FROM party_proposal WHERE party_id = :partyId AND status = 'WAITING'", nativeQuery = true)
    PartyProposal findWaitingProposal(@Param(value = "partyId") Long partyId);

    List<PartyProposal> findByProposer(User user);
}

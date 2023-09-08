package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalRepository extends JpaRepository<PartyProposal, Long> {

    @Query(value = "SELECT CASE WHEN p.driver_agreement = 'ACCEPT'\n"
        + "    AND NOT EXISTS (SELECT * FROM party_agreement a WHERE a.proposal_id = p.id AND (a.status = 'REFUSE' OR a.status = 'WAITING'))\n"
        + "    THEN true ELSE false END\n"
        + "    FROM party_proposal p WHERE p.id = :proposalId", nativeQuery = true)
    Integer isUnanimity(@Param(value = "proposalId") Long proposalId);

    List<PartyProposal> findByProposer(User user);

    PartyProposal findByPartyAndStatus(Party party, ProposalStatus status);

    Boolean existsByPartyAndStatus(Party party, ProposalStatus status);
}

package mallang_trip.backend.domain.party.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.constant.ProposalType;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalRepository extends JpaRepository<PartyProposal, Long> {

	@Query(value = "SELECT CASE WHEN p.driver_agreement = 'ACCEPT'\n"
		+ "    AND NOT EXISTS (SELECT * FROM party_proposal_agreement a "
		+ "                     WHERE a.proposal_id = p.id "
		+ "                     AND a.deleted = 'false' "
		+ "                     AND (a.status = 'REFUSE' OR a.status = 'WAITING'))\n"
		+ "    THEN 'true' ELSE 'false' END\n"
		+ "    FROM party_proposal p WHERE p.id = :proposalId", nativeQuery = true)
	Boolean isUnanimity(@Param(value = "proposalId") Long proposalId);

	Optional<PartyProposal> findByPartyAndStatus(Party party, ProposalStatus status);

	@Query(value = "SELECT * FROM party_proposal\n"
		+ "WHERE status='WAITING'\n"
		+ "AND created_at < :time", nativeQuery = true)
	List<PartyProposal> findExpiredProposal(@Param(value = "time") String time);

	List<PartyProposal> findByProposerAndTypeAndStatus(User user, ProposalType type,
		ProposalStatus status);

	Boolean existsByProposerAndTypeAndStatus(User user, ProposalType type,
		ProposalStatus status);

	Boolean existsByPartyAndProposerAndTypeAndStatus(Party party, User user, ProposalType type,
		ProposalStatus status);

}

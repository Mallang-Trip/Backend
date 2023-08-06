package mallang_trip.backend.repository.party;

import mallang_trip.backend.domain.entity.party.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalRepository extends JpaRepository<PartyProposal, Long> {

}

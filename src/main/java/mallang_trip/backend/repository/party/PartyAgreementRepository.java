package mallang_trip.backend.repository.party;

import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyAgreementRepository extends JpaRepository<PartyAgreement, Long> {

    Boolean deleteByProposal(PartyProposal proposal);
}

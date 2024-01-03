package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyAgreementRepository extends JpaRepository<PartyAgreement, Long> {

    void deleteByProposal(PartyProposal proposal);

    List<PartyAgreement> findByProposal(PartyProposal proposal);

    PartyAgreement findByMembersAndProposal(PartyMember members, PartyProposal proposal);
}

package mallang_trip.backend.repository.party;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.entity.party.PartyProposalAgreement;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalAgreementRepository extends JpaRepository<PartyProposalAgreement, Long> {

    void deleteByProposal(PartyProposal proposal);

    List<PartyProposalAgreement> findByProposal(PartyProposal proposal);

    Optional<PartyProposalAgreement> findByMemberAndProposal(PartyMember member, PartyProposal proposal);

    void deleteByProposalAndMember(PartyProposal proposal, PartyMember member);
}

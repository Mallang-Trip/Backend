package mallang_trip.backend.domain.party.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.party.entity.PartyProposalAgreement;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyProposalAgreementRepository extends JpaRepository<PartyProposalAgreement, Long> {

    List<PartyProposalAgreement> findByProposal(PartyProposal proposal);

    Optional<PartyProposalAgreement> findByMemberAndProposal(PartyMember member, PartyProposal proposal);

    void deleteByProposalAndMember(PartyProposal proposal, PartyMember member);
}

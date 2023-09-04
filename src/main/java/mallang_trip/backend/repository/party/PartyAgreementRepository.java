package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyAgreementRepository extends JpaRepository<PartyAgreement, Long> {

    Boolean deleteByProposal(PartyProposal proposal);

    List<PartyAgreement> findByProposal(PartyProposal proposal);

    PartyAgreement findByUserAndProposal(User user, PartyProposal proposal);
}

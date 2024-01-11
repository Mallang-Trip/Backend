package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyMemberCompanion;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyMemberCompanionRepository extends JpaRepository<PartyMemberCompanion, Long> {

	List<PartyMemberCompanion> findByMember(PartyMember member);

	List<PartyMemberCompanion> findByProposal(PartyProposal proposal);
}

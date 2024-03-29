package mallang_trip.backend.domain.party.repository;

import java.util.List;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyMemberCompanion;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyMemberCompanionRepository extends JpaRepository<PartyMemberCompanion, Long> {

	List<PartyMemberCompanion> findByMember(PartyMember member);

	List<PartyMemberCompanion> findByProposal(PartyProposal proposal);
}

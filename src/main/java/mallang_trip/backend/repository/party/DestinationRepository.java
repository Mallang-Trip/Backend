package mallang_trip.backend.repository.party;

import mallang_trip.backend.domain.entity.party.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

}

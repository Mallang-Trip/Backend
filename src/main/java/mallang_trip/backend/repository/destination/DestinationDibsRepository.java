package mallang_trip.backend.repository.destination;

import java.util.List;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationDibs;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationDibsRepository extends JpaRepository<DestinationDibs, Long> {

    Boolean existsByDestinationAndUser(Destination destination, User user);

    List<DestinationDibs> findAllByUserOrderByUpdatedAtDesc(User user);

    void deleteByDestinationAndUser(Destination destination, User user);
}

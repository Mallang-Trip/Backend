package mallang_trip.backend.domains.destination.repository;

import java.util.List;
import mallang_trip.backend.domains.destination.entity.Destination;
import mallang_trip.backend.domains.destination.entity.DestinationDibs;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationDibsRepository extends JpaRepository<DestinationDibs, Long> {

    Boolean existsByDestinationAndUser(Destination destination, User user);

    List<DestinationDibs> findAllByUserOrderByUpdatedAtDesc(User user);

    void deleteByDestinationAndUser(Destination destination, User user);
}

package mallang_trip.backend.repository.destination;

import java.util.List;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationReview;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationReviewRepository extends JpaRepository<DestinationReview, Long> {

    @Query("SELECT AVG(r.rate) FROM DestinationReview r WHERE r.destination = ?1 AND r.deleted = false")
    Double getAvgRating(Destination destination);

    List<DestinationReview> findAllByDestinationOrderByUpdatedAtDesc(Destination destination);

    Boolean existsByDestinationAndUser(Destination destination, User user);
}

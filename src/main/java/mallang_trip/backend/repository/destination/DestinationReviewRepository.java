package mallang_trip.backend.repository.destination;

import java.util.List;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationReviewRepository extends JpaRepository<DestinationReview, Long> {

    @Query("SELECT AVG(r.rate) FROM DestinationReview r WHERE r.destination = ?1")
    Double getAvgRating(Destination destination);

    List<DestinationReview> findAllByDestination(Destination destination);
}

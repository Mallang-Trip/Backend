package mallang_trip.backend.repository.driver;

import java.util.List;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverReview;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverReviewRepository extends JpaRepository<DriverReview, Long> {

    @Query("SELECT AVG(r.rate) FROM DriverReview r WHERE r.driver = ?1")
    Double getAvgRating(Driver driver);

    Boolean existsByDriverAndUser(Driver driver, User user);

    List<DriverReview> findAllByDriver(Driver driver);
}

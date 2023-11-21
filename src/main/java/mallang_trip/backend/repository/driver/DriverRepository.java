package mallang_trip.backend.repository.driver;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByStatus(DriverStatus status);

    List<Driver> findAllByRegionAndDeletedAndStatus(String region, Boolean deleted,
        DriverStatus status);

    Optional<Driver> findByIdAndDeletedAndStatus(Long id, Boolean deleted, DriverStatus status);
}

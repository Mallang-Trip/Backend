package mallang_trip.backend.domain.driver.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByStatus(DriverStatus status);

    List<Driver> findAllByRegionAndStatus(String region, DriverStatus status);

    Optional<Driver> findByIdAndStatus(Long id, DriverStatus status);

    Boolean existsByUser(User user);

    Optional<Driver> findByUser(User user);
}

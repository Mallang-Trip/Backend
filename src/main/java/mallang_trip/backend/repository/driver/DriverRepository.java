package mallang_trip.backend.repository.driver;

import java.util.List;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.domain.entity.driver.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByStatus(DriverStatus status);

    List<Driver> findAllByRegion(String region);
}

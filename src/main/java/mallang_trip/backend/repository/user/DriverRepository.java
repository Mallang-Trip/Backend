package mallang_trip.backend.repository.user;

import java.util.List;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.domain.entity.user.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByStatus(DriverStatus status);
}

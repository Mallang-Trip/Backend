package mallang_trip.backend.repository.user;

import java.util.List;
import mallang_trip.backend.domain.entity.user.Driver;
import mallang_trip.backend.domain.entity.user.DriverPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverPriceRepository extends JpaRepository<DriverPrice, Long> {

    List<DriverPrice> findAllByDriver(Driver driver);

    void deleteAllByDriver(Driver driver);
}

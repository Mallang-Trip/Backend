package mallang_trip.backend.repository.driver;

import java.util.List;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverPriceRepository extends JpaRepository<DriverPrice, Long> {

    List<DriverPrice> findAllByDriver(Driver driver);

    void deleteAllByDriver(Driver driver);
}

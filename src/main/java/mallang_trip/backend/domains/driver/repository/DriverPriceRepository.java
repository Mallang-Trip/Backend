package mallang_trip.backend.domains.driver.repository;

import java.util.List;
import mallang_trip.backend.domains.driver.entity.Driver;
import mallang_trip.backend.domains.driver.entity.DriverPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverPriceRepository extends JpaRepository<DriverPrice, Long> {

    List<DriverPrice> findAllByDriver(Driver driver);

    void deleteAllByDriver(Driver driver);
}

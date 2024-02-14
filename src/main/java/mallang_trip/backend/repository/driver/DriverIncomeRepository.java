package mallang_trip.backend.repository.driver;

import java.util.List;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverIncomeRepository extends JpaRepository<DriverIncome, Long> {

	List<DriverIncome> findByDriverOrderByCreatedAtDesc(Driver driver);
}

package mallang_trip.backend.domains.driver.repository;

import java.util.List;
import mallang_trip.backend.domains.driver.entity.Driver;
import mallang_trip.backend.domains.driver.entity.DriverIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverIncomeRepository extends JpaRepository<DriverIncome, Long> {

	List<DriverIncome> findByDriverOrderByCreatedAtDesc(Driver driver);
}

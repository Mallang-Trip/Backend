package mallang_trip.backend.domain.driver.repository;

import java.util.List;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.entity.DriverIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverIncomeRepository extends JpaRepository<DriverIncome, Long> {

	List<DriverIncome> findByDriverOrderByCreatedAtDesc(Driver driver);
}

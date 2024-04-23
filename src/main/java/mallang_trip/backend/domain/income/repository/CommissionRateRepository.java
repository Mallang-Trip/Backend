package mallang_trip.backend.domain.income.repository;

import mallang_trip.backend.domain.income.entity.CommissionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {

}

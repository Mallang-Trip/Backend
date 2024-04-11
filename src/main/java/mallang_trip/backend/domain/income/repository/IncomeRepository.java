package mallang_trip.backend.domain.income.repository;

import java.time.LocalDate;
import java.util.List;
import mallang_trip.backend.domain.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

	@Query(value = "SELECT di.*\n"
		+ "FROM driver_income di \n"
		+ "		JOIN party p ON di.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "		AND di.deleted = false\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Income> findByDriver(@Param("driver_id") Long driverId);

	@Query(value = "SELECT di.*\n"
		+ "FROM driver_income di\n"
		+ "		JOIN party p ON di.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "		AND di.deleted = false\n"
		+ "		AND di.remitted = true\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Income> findRemittedIncomesByDriver(@Param("driver_id") Long driverId);

	@Query(value = "SELECT di.amount - di.commission\n"
		+ "FROM driver_income di\n"
		+ "    JOIN party p ON di.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "    AND di.deleted = false\n"
		+ "    AND p.end_date >= :start_date\n"
		+ "    AND p.end_date < :end_date\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Integer> findByDriverAndPeriod(@Param("driver_id") Long driverId,
		@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate);

}

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

	@Query(value = "SELECT i.*\n"
		+ "FROM income i \n"
		+ "		JOIN party p ON i.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "		AND i.deleted = false\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Income> findByDriver(@Param("driver_id") Long driverId);

	@Query(value = "SELECT i.*\n"
		+ "FROM income i\n"
		+ "		JOIN party p ON i.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "		AND i.deleted = false\n"
		+ "		AND i.remitted = true\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Income> findRemittedIncomesByDriver(@Param("driver_id") Long driverId);

	@Query(value = "SELECT i.*\n"
		+ "FROM income i\n"
		+ "    JOIN party p ON i.party_id = p.id\n"
		+ "WHERE p.driver_id = :driver_id\n"
		+ "    AND i.deleted = false\n"
		+ "    AND p.end_date >= :start_date\n"
		+ "    AND p.end_date < :end_date\n"
		+ "ORDER BY p.end_Date DESC;", nativeQuery = true)
	List<Income> findByDriverAndPeriod(@Param("driver_id") Long driverId,
		@Param("start_date") String startDate, @Param("end_date") String endDate);

	@Query(value = "SELECT i.*\n"
		+ "FROM income i JOIN party p ON i.party_id = p.id\n"
		+ "WHERE i.deleted = false\n"
		+ "ORDER BY p.end_date DESC;", nativeQuery = true)
	List<Income> findAllOrderByEndDateDesc();

	@Query(value = "SELECT i.*\n"
		+ "FROM income i\n"
		+ "    JOIN party p ON i.party_id = p.id\n"
		+ "WHERE i.deleted = false\n"
		+ "    AND p.end_date >= :start_date\n"
		+ "    AND p.end_date < :end_date\n"
		+ "ORDER BY p.end_Date DESC;", nativeQuery = true)
	List<Income> findByPeriod(@Param("start_date") String startDate,
		@Param("end_date") String endDate);
}

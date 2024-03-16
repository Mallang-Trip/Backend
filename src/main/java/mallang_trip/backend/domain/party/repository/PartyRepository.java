package mallang_trip.backend.domain.party.repository;

import java.util.List;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.party.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END FROM party\n"
		+ "WHERE driver_id = :driverId\n"
		+ "AND NOT (status LIKE 'CANCELED%')\n"
		+ "AND start_date = :date", nativeQuery = true)
	Boolean existsValidPartyByDriverAndStartDate(@Param(value = "driverId") Long driverId,
		@Param(value = "date") String startDate);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END FROM party p\n"
		+ "JOIN party_member m ON p.id = m.party_id\n"
		+ "WHERE m.user_id = :userId AND NOT (p.status LIKE 'CANCELED%') AND p.start_date = :date",
		nativeQuery = true)
	Boolean existsValidPartyByUserAndStartDate(@Param(value = "userId") Long userId,
		@Param(value = "date") String startDate);

	@Query(value = "SELECT * FROM party\n"
		+ "WHERE start_date <= :today AND (status = 'RECRUITING' OR status = 'WAITING_JOIN_APPROVAL')", nativeQuery = true)
	List<Party> findExpiredRecruitingParties(@Param(value = "today") String today);

	@Query(value = "SELECT * FROM party\n"
		+ "WHERE start_date = :today AND (status = 'SEALED' OR status = 'WAITING_COURSE_CHANGE_APPROVAL')", nativeQuery = true)
	List<Party> findDayOfTravelParties(@Param(value = "today") String today);

	@Query(value = "SELECT * FROM party\n"
		+ "WHERE start_date < :today AND status = 'DAY_OF_TRAVEL' ", nativeQuery = true)
	List<Party> findFinishingParties(@Param(value = "today") String today);

	@Query(value = "SELECT * FROM party\n"
		+ "WHERE start_date = :yesterday AND status = 'FINISHED' ", nativeQuery = true)
	List<Party> findFinishedParties(@Param(value = "yesterday") String yesterday);

	List<Party> findByStatus(PartyStatus status);

	@Query(value = "SELECT * FROM party WHERE status LIKE 'CANCELED_%'", nativeQuery = true)
	List<Party> findByStatusStartWithCanceled();

	List<Party> findByRegionAndStatus(String region, PartyStatus status);

	List<Party> findByDriver(Driver driver);

	@Query(value = "SELECT COUNT(*) > 0\n"
		+ "FROM party p JOIN party_member m\n"
		+ "ON p.id = m.party_id\n"
		+ "WHERE m.user_id = :user_id\n"
		+ "	 AND m.deleted = 'false'\n"
		+ "  AND (p.status = 'RECRUITING'\n"
		+ "   OR p.status = 'WAITING_JOIN_APPROVAL'\n"
		+ "   OR p.status = 'SEALED'\n"
		+ "   OR p.status = 'WAITING_COURSE_CHANGE_APPROVAL'\n"
		+ "   OR p.status = 'DAY_OF_TRAVEL')", nativeQuery = true)
	boolean isOngoingPartyExists(@Param(value = "user_id") Long userId);

	Integer countByDriverAndStatus(Driver driver, PartyStatus status);
}

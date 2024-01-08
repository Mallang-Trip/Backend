package mallang_trip.backend.repository.party;

import java.time.LocalDate;
import java.util.List;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.party.Party;
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

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END \n"
        + "FROM party p JOIN party_members m ON p.id = m.party_id\n"
        + "WHERE m.user_id = :userId AND NOT (status LIKE 'CANCELED%') AND p.start_date = :date",
    nativeQuery = true)
    Boolean existsValidPartyByUserAndStartDate(@Param(value = "userId") Long userId,
        @Param(value = "date") String startDate);

    @Query(value = "SELECT * FROM party\n"
        + "WHERE start_date <= :today AND (status = 'RECRUITING' OR status = 'WAITING_JOIN_APPROVAL')", nativeQuery = true)
    List<Party> findExpiredRecruitingParties(@Param(value = "today") String today);

    @Query(value = "SELECT * FROM party\n"
        + "WHERE start_date <= :today AND status = 'WAITING_COURSE_CHANGE_APPROVAL'", nativeQuery = true)
    List<Party> findExpiredWaitingCourseChangeApprovalParties(@Param(value = "today") String today);

    @Query(value = "SELECT * FROM party\n"
        + "WHERE start_date = :today AND status = 'SEALED' ", nativeQuery = true)
    List<Party> findDayOfTravelParties(@Param(value = "today") String today);

    @Query(value = "SELECT * FROM party\n"
        + "WHERE start_date < :today AND status = 'DAY_OF_TRAVEL' ", nativeQuery = true)
    List<Party> findFinishedParties(@Param(value = "today") String today);

    List<Party> findByStatus(PartyStatus status);

    List<Party> findByRegionAndStatus(String region, PartyStatus status);
}

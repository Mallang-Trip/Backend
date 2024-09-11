package mallang_trip.backend.domain.reservation.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

	Optional<Reservation> findByMemberAndStatus(PartyMember member, ReservationStatus status);

	@Query(value = "SELECT * FROM reservation\n"
		+ "WHERE party_member_id = :member_id\n"
		+ "    AND (status = 'PAYMENT_FAILED' OR status = 'PAYMENT_COMPLETE');", nativeQuery = true)
	Optional<Reservation> findPaymentCompletedOrFailedByMember(@Param(value = "member_id") Long memberId);

	@Query(value = "SELECT r.*\n"
		+ "FROM reservation r\n"
		+ "    JOIN party_member pm ON r.party_member_id = pm.id\n"
		+ "    JOIN user u ON pm.user_id = u.id\n"
		+ "WHERE u.id = :user_id\n"
		+ "ORDER BY r.updated_at DESC;", nativeQuery = true)
	List<Reservation> findByUser(@Param(value = "user_id") Long userId);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END\n"
		+ "FROM reservation r\n"
		+ "    JOIN party_member pm ON r.party_member_id = pm.id\n"
		+ "    JOIN user u ON pm.user_id = u.id\n"
		+ "WHERE u.id = :user_id AND r.status = 'PAYMENT_FAILED';" , nativeQuery = true)
	Boolean isPaymentFailedExistsByUser(@Param(value = "user_id") Long userId);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END\n"
		+ "FROM reservation r\n"
		+ "    JOIN party_member pm ON r.party_member_id = pm.id\n"
		+ "    JOIN user u ON pm.user_id = u.id\n"
		+ "WHERE u.id = :user_id AND r.penalty_amount IS NOT NULL;" , nativeQuery = true)
	Boolean isPenaltyExistsByUser(@Param(value = "user_id") Long userId);
}

package mallang_trip.backend.domain.reservation.repository;

import java.util.Optional;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	Optional<Reservation> findByMemberAndStatus(PartyMember member, ReservationStatus status);

	@Query(value = "SELECT * FROM reservation\n"
		+ "WHERE party_member_id = :member_id\n"
		+ "    AND (status = 'PAYMENT_FAILED' OR status = 'PAYMENT_COMPLETE');", nativeQuery = true)
	Optional<Reservation> findPaymentCompletedOrFailedByMember(@Param(value = "member_id") Long memberId);

	Optional<Reservation> findByMember(PartyMember member);
}

package mallang_trip.backend.domain.reservation.repository;

import java.util.Optional;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	Optional<Reservation> findByMemberAndStatus(PartyMember member, ReservationStatus status);
}

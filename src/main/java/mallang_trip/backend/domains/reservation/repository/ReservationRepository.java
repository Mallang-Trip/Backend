package mallang_trip.backend.domains.reservation.repository;

import java.util.Optional;
import mallang_trip.backend.domains.reservation.constant.ReservationStatus;
import mallang_trip.backend.domains.party.entity.PartyMember;
import mallang_trip.backend.domains.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	Optional<Reservation> findByMemberAndStatus(PartyMember member, ReservationStatus status);
}

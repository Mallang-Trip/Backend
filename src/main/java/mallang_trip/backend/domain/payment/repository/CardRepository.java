package mallang_trip.backend.domain.payment.repository;

import java.util.Optional;
import mallang_trip.backend.domain.payment.entity.Card;
import mallang_trip.backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByPayment(Payment payment);
}

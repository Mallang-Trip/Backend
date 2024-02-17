package mallang_trip.backend.repository.payment;

import java.util.Optional;
import mallang_trip.backend.domain.entity.payment.Card;
import mallang_trip.backend.domain.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByPayment(Payment payment);
}

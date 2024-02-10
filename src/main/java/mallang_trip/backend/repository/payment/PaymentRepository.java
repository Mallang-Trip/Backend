package mallang_trip.backend.repository.payment;

import java.util.Optional;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUser(User user);
}

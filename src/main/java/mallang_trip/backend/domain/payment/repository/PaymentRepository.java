package mallang_trip.backend.domain.payment.repository;

import java.util.Optional;
import mallang_trip.backend.domain.payment.entity.Payment;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUser(User user);
}

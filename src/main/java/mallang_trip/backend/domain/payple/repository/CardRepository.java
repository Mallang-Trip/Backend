package mallang_trip.backend.domain.payple.repository;

import java.util.Optional;
import mallang_trip.backend.domain.payple.entity.Card;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

	Optional<Card> findByUser(User user);
}

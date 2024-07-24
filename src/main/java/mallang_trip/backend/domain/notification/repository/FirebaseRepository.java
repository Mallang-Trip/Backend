package mallang_trip.backend.domain.notification.repository;

import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirebaseRepository extends JpaRepository<Firebase, Long> {

    // find by user and token not null
    Optional<Firebase> findByUserAndTokensNotNull(User user);

    Optional<Firebase> findByUser(User user);

    // exists by user
    Boolean existsByUser(User user);
}

package mallang_trip.backend.repository;

import java.util.Optional;
import mallang_trip.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByLoginId(String loginId);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);

    Optional<User> findByLoginId(String id);
}

package mallang_trip.backend.domains.user.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByLoginId(String loginId);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);

    Optional<User> findByLoginId(String id);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findByNicknameContainingIgnoreCaseAndDeleted(String nickname, Boolean deleted);
}

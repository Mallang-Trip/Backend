package mallang_trip.backend.domains.admin.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domains.admin.constant.SuspensionStatus;
import mallang_trip.backend.domains.admin.entity.Suspension;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {

	Boolean existsByUserAndStatus(User user, SuspensionStatus status);

	Optional<Suspension> findByUserAndStatus(User user, SuspensionStatus status);

	List<Suspension> findByStatus(SuspensionStatus status);
}

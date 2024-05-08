package mallang_trip.backend.domain.admin.repository;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.admin.constant.SuspensionStatus;
import mallang_trip.backend.domain.admin.entity.Report;
import mallang_trip.backend.domain.admin.entity.Suspension;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {

	Boolean existsByUserAndStatus(User user, SuspensionStatus status);

	List<Suspension> findByUserAndStatus(User user, SuspensionStatus status);

	List<Suspension> findByStatus(SuspensionStatus status);

	// report_id로 suspension 조회
	Optional<Suspension> findByReportId(Long reportId);

	Boolean existsByReport(Report report);
}

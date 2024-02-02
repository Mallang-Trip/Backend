package mallang_trip.backend.repository.admin;

import java.util.List;
import mallang_trip.backend.constant.ReportStatus;
import mallang_trip.backend.domain.entity.admin.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);
}

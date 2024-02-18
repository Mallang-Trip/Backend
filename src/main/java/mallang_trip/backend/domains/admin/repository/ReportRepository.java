package mallang_trip.backend.domains.admin.repository;

import java.util.List;
import mallang_trip.backend.domains.admin.constant.ReportStatus;
import mallang_trip.backend.domains.admin.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);
}

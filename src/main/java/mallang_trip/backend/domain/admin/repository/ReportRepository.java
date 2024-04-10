package mallang_trip.backend.domain.admin.repository;

import java.util.List;
import java.util.Optional;

import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

	Optional<Report> findById(Long id);
}

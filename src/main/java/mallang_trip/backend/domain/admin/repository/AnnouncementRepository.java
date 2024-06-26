package mallang_trip.backend.domain.admin.repository;

import mallang_trip.backend.domain.admin.constant.AnnouncementType;
import mallang_trip.backend.domain.admin.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

	Page<Announcement> findByTypeOrderByCreatedAtDesc(AnnouncementType type, Pageable pageable);

	Optional<Announcement> findById(Long id);
}

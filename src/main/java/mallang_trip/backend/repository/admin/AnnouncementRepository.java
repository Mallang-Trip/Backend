package mallang_trip.backend.repository.admin;

import mallang_trip.backend.constant.AnnouncementType;
import mallang_trip.backend.domain.entity.admin.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

	Page<Announcement> findByTypeOrderByCreatedAtDesc(AnnouncementType type, Pageable pageable);
}

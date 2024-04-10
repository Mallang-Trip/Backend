package mallang_trip.backend.domain.admin.repository;

import mallang_trip.backend.domain.admin.constant.ObjectionStatus;
import mallang_trip.backend.domain.admin.entity.Objection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObjectionRepository extends JpaRepository<Objection, Long> {

    List<Objection> findByStatusOrderByCreatedAtDesc(ObjectionStatus status);
}

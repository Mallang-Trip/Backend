package mallang_trip.backend.domain.region.repository;

import java.util.List;
import mallang_trip.backend.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

	Boolean existsByName(String name);

	List<Region> findAllByOrderByNameAsc();

	List<Region> findByNameContaining(String keyword);
}

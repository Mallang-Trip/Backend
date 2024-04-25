package mallang_trip.backend.domain.party.repository;

import mallang_trip.backend.domain.party.entity.PartyRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRegionRepository extends JpaRepository<PartyRegion,Long> {

    Optional<PartyRegion> findByRegion(String region);

    // 가나다 순 모든 지역 조회
    List<PartyRegion> findAllByOrderByRegionAsc();

    // 지역 존재 여부 deleted = false 인 것만 조회
    Boolean existsByRegion(String region);
}

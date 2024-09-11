package mallang_trip.backend.domain.reservation.repository;

import mallang_trip.backend.domain.reservation.entity.PromotionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionCodeRepository extends JpaRepository<PromotionCode, Long> {

    Optional<PromotionCode> findByCode(String code);

    Optional<PromotionCode> findById(Long id);
}

package mallang_trip.backend.domain.reservation.repository;

import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus;
import mallang_trip.backend.domain.reservation.entity.PromotionCode;
import mallang_trip.backend.domain.reservation.entity.UserPromotionCode;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPromotionCodeRepository extends JpaRepository<UserPromotionCode, Long> {

    Optional<UserPromotionCode> findByUserAndCodeAndStatus(User user, PromotionCode code, UserPromotionCodeStatus status);

//    List<UserPromotionCode> findByCode(PromotionCode code);
    Optional<UserPromotionCode> findByIdAndStatus(Long id, UserPromotionCodeStatus status);

}
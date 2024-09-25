package mallang_trip.backend.domain.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE user_promotion_code SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class UserPromotionCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "party_member_id", nullable = false)
//    private PartyMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_code_id", nullable = false)
    private PromotionCode code;

    @Enumerated(EnumType.STRING)
    private UserPromotionCodeStatus status;

    public void changeStatus(UserPromotionCodeStatus status) {
        this.status = status;
    }
}

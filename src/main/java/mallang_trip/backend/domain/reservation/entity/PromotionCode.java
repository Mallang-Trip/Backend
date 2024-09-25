package mallang_trip.backend.domain.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.reservation.dto.PromotionCodeCreateRequest;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE promotion_code SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PromotionCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String code;    // 프로모션 코드

    @Column
    private Boolean free;   // 무료 여부

    @Column
    private Integer discountPrice;   // 할인 금액

    @Column
    private Integer discountRate;   // 할인율

    @Column
    private Integer minimumPrice;   // 최소 금액

    @Column
    private Integer maximumPrice;   // 최대 금액

    @Column
    private Integer maximumDiscountPrice;   // 최대 할인 금액

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;   // 만료일

    @Column
    private Integer count;   // 발급 수

    @Column
    @Builder.Default
    private Integer usedCount = 0;   // 사용 수

    public void use() {
        this.usedCount++;
    }

    public void cancel() {
        this.usedCount--;
    }

    public Boolean isAvailable() {
        return this.count > this.usedCount;
    }

    public Boolean isExpired() {
        return this.endDate.isBefore(LocalDate.now());
    }

    public void modify(PromotionCodeCreateRequest request) {
        this.code = request.getCode();
        this.free = request.getFree();
        this.discountPrice = request.getDiscountPrice();
        this.discountRate = request.getDiscountRate();
        this.minimumPrice = request.getMinimumPrice();
        this.maximumPrice = request.getMaximumPrice();
        this.maximumDiscountPrice = request.getMaximumDiscountPrice();
        this.endDate = LocalDate.parse(request.getEndDate());
        this.count = request.getCount();
    }

}

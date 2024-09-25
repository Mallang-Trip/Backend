package mallang_trip.backend.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.reservation.entity.PromotionCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PromotionCodeResponse {
    private Long id;
    private String code;
    private Boolean free;
    private Integer discountPrice;
    private Integer discountRate;
    private Integer minimumPrice;
    private Integer maximumPrice;
    private Integer maximumDiscountPrice;
    private LocalDate endDate;
    private Integer count;
    private Integer usedCount;

    public static PromotionCodeResponse of(PromotionCode promotionCode) {
        return PromotionCodeResponse.builder()
            .id(promotionCode.getId())
            .code(promotionCode.getCode())
            .free(promotionCode.getFree())
            .discountPrice(promotionCode.getDiscountPrice())
            .discountRate(promotionCode.getDiscountRate())
            .minimumPrice(promotionCode.getMinimumPrice())
            .maximumPrice(promotionCode.getMaximumPrice())
            .maximumDiscountPrice(promotionCode.getMaximumDiscountPrice())
            .endDate(promotionCode.getEndDate())
            .count(promotionCode.getCount())
            .usedCount(promotionCode.getUsedCount())
            .build();
    }
}

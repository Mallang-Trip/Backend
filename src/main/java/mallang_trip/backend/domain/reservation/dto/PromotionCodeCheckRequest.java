package mallang_trip.backend.domain.reservation.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionCodeCheckRequest {
    @ApiModelProperty(value = "프로모션 코드", required = true)
    private String code;
    @ApiModelProperty(value = "현재 파티 가격", required = true)
    private Long price;
}

package mallang_trip.backend.domain.reservation.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder
public class PromotionCodeUseRequest {
    @ApiModelProperty(value = "프로모션 코드", required = true)
    private String code;
}

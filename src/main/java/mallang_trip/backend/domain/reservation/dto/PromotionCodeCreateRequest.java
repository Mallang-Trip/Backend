package mallang_trip.backend.domain.reservation.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionCodeCreateRequest {
    @ApiModelProperty(value = "프로모션 코드", required = true)
    private String code;
    @ApiModelProperty(value = "무료 여부", required = true) // 일단 현재는 이것만 사용하는 것으로
    private Boolean free;



    // 아래 것들은 service 단에서 TODO
    @ApiModelProperty(value = "할인 가격", required = true)
    private Integer discountPrice;
    @ApiModelProperty(value = "할인율", required = true)
    private Integer discountRate;
    @ApiModelProperty(value = "최소 가격", required = true)
    private Integer minimumPrice;
    @ApiModelProperty(value = "최대 가격", required = true)
    private Integer maximumPrice;
    @ApiModelProperty(value = "최대 할인 가격", required = true)
    private Integer maximumDiscountPrice;
    @ApiModelProperty(value = "종료 날짜", required = true)
    private String endDate;
    @ApiModelProperty(value = "사용 가능 횟수", required = true)
    private Integer count;
}

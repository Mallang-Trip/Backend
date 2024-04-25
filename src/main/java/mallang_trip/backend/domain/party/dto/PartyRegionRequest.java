package mallang_trip.backend.domain.party.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class PartyRegionRequest {

    @NotBlank
    @ApiModelProperty(value = "가고 싶은 지역", required = true)
    private String region;

    @NotBlank
    @ApiModelProperty(value = "지역 이미지", required = true)
    private String regionImg;
}

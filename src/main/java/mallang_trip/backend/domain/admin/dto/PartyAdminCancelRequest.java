package mallang_trip.backend.domain.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyAdminCancelRequest {
    @ApiModelProperty(value = "파티 취소 사유", required = true, example = "사유")
    String reason;
}

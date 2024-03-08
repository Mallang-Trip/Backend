package mallang_trip.backend.domain.user.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangePasswordRequest {

    @NotBlank
    @ApiModelProperty(value = "기존 비밀번호", required = true)
    private String before;

    @NotBlank
    @ApiModelProperty(value = "새 비밀번호", required = true)
    private String after;
}

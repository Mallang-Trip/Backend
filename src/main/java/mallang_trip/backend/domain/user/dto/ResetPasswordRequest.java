package mallang_trip.backend.domain.user.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordRequest {

    @NotBlank
    @ApiModelProperty(value = "인증번호", required = true)
    private String code;

    @NotBlank
    @ApiModelProperty(value = "휴대폰 번호", required = true)
    private String phoneNumber;

    @NotBlank
    @ApiModelProperty(value = "새 비밀번호", required = true)
    private String newPassword;
}

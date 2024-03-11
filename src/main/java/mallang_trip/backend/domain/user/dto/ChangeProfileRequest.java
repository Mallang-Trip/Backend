package mallang_trip.backend.domain.user.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangeProfileRequest {

    @NotBlank
    @ApiModelProperty(value = "이메일", required = true)
    private String email;

    @NotBlank
    @ApiModelProperty(value = "닉네임", required = true)
    private String nickname;

    @ApiModelProperty(value = "자기소개", required = false)
    private String introduction;

    @ApiModelProperty(value = "프로필 이미지 URL", required = false)
    private String profileImg;
}

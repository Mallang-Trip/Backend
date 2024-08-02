package mallang_trip.backend.domain.user.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @ApiModelProperty(value = "로그인 아이디", required = true)
    private String id;

    @NotBlank
    @ApiModelProperty(value = "비밀번호", required = true)
    private String password;

    @NotBlank
    @Email
    @ApiModelProperty(value = "이메일", required = true)
    private String email;

    @NotBlank
    @ApiModelProperty(value = "닉네임", required = true)
    private String nickname;

    @NotBlank
    @ApiModelProperty(value = "본인인증 imp_uid", required = true)
    private String impUid;

    @ApiModelProperty(value = "자기소개", required = false)
    private String introduction;

    @ApiModelProperty(value = "프로필 이미지 URL", required = false)
    private String profileImg;
}

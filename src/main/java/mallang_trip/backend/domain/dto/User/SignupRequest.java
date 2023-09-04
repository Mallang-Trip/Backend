package mallang_trip.backend.domain.dto.User;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.constant.Country;
import mallang_trip.backend.constant.Gender;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @ApiModelProperty(value = "로그인 아이디")
    private String id;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    @ApiModelProperty(value = "yyyyMMdd")
    private String birthday;

    @NotBlank
    @ApiModelProperty(value = "local/foreigner")
    private String country;

    @NotBlank
    @ApiModelProperty(value = "male/female")
    private String gender;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String nickname;

    @ApiModelProperty(value = "생략가능")
    private String introduction;

    @ApiModelProperty(value = "생략가능")
    private String profileImg;

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
            .loginId(id)
            .password(passwordEncoder.encode(password))
            .email(email)
            .name(name)
            .birthday(parsingBirthday(birthday))
            .country(Country.from(country))
            .gender(Gender.from(gender))
            .phoneNumber(phoneNumber)
            .nickname(nickname)
            .introduction(introduction)
            .profileImage(profileImg)
            .role(Role.ROLE_USER)
            .build();
    }

    private LocalDate parsingBirthday(String birthday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            return LocalDate.parse(birthday, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}

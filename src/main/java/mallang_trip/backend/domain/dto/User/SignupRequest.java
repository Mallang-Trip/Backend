package mallang_trip.backend.domain.dto.User;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String birthday;

    @NotBlank
    private String country;

    @NotBlank
    private String gender;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String nickname;

    private String introduction;

    public User toUser(String image, PasswordEncoder passwordEncoder) {
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
            .profileImage(image)
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

    public static SignupRequest jsonToSignupRequest(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
            SignupRequest request = objectMapper.readValue(json, new TypeReference<>() {
            });
            return request;
        } catch (JsonProcessingException e){
            throw new BaseException(Bad_Request);
        }
    }
}

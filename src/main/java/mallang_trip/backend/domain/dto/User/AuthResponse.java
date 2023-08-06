package mallang_trip.backend.domain.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.domain.entity.user.User;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private Long userId;
    private String loginId;
    private String email;
    private String name;
    private String birthday;
    private String country;
    private String gender;
    private String nickname;
    private String phoneNumber;
    private String role;
    private String introduction;
    private String profileImg;
    private Boolean suspended;
    private Boolean deleted;

    public static AuthResponse of(User user){
        return AuthResponse.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .name(user.getName())
            .birthday(user.getBirthday().toString())
            .country(user.getCountry().toString())
            .gender(user.getGender().toString())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber())
            .role(user.getRole().toString())
            .introduction(user.getIntroduction())
            .profileImg(user.getProfileImage())
            .suspended(user.getSuspended())
            .deleted(user.getDeleted())
            .build();
    }
}

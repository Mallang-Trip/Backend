package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.user.entity.User;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoForAdminResponse {

    private Long userId;
    private String loginId;
    private String name;
    private String userNickname;
    private String phoneNumber;
    private Integer suspensionDuration;
    private LocalDateTime createdAt;
    private String Introduction;
    private String profileImg;
    private String role;

    // If the user is a driver, the following fields are also included
    private String driverRegion;

    public static UserInfoForAdminResponse of(User user, Integer duration) {
        return UserInfoForAdminResponse.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .name(user.getName())
            .userNickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber())
            .suspensionDuration(duration)
            .profileImg(user.getProfileImage())
            .Introduction(user.getIntroduction())
            .createdAt(user.getCreatedAt())
            .role(user.getRole().toString())
            .build();
    }

    public static UserInfoForAdminResponse of(User user, Integer duration, String driverRegion) {
        return UserInfoForAdminResponse.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .name(user.getName())
            .userNickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber())
            .suspensionDuration(duration)
            .profileImg(user.getProfileImage())
            .Introduction(user.getIntroduction())
            .createdAt(user.getCreatedAt())
            .role(user.getRole().toString())
            .driverRegion(driverRegion)
            .build();
    }
}

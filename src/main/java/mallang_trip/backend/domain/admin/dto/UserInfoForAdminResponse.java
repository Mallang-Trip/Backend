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
    private String userNickname;
    private Integer suspensionDuration;
    private LocalDateTime createdAt;
    private String Introduction;
    private String profileImg;

    public static UserInfoForAdminResponse of(User user, Integer duration) {
        return UserInfoForAdminResponse.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .userNickname(user.getNickname())
            .suspensionDuration(duration)
            .profileImg(user.getProfileImage())
            .Introduction(user.getIntroduction())
            .createdAt(user.getCreatedAt())
            .build();
    }
}

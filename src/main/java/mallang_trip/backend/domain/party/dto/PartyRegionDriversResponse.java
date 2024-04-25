package mallang_trip.backend.domain.party.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.dto.UserInfoForAdminResponse;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
public class PartyRegionDriversResponse {


    // Driver 프로필 용
    private Long driverId;

    // User 프로필 용
    private Long userId;
    private String loginId;
    private String userNickname;
    private Integer suspensionDuration;
    private LocalDateTime createdAt;
    private String Introduction;
    private String profileImg;

    public static PartyRegionDriversResponse of(Driver driver, Integer duration) {
        return PartyRegionDriversResponse.builder()
                .driverId(driver.getId())
                .userId(driver.getUser().getId())
                .loginId(driver.getUser().getLoginId())
                .userNickname(driver.getUser().getNickname())
                .suspensionDuration(duration)
                .createdAt(driver.getCreatedAt())
                .Introduction(driver.getUser().getIntroduction())
                .profileImg(driver.getUser().getProfileImage())
                .build();
    }
}

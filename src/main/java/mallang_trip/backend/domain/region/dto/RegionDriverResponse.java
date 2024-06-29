package mallang_trip.backend.domain.region.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.party.dto.PartyRegionDriversResponse;

@Builder
@Getter
public class RegionDriverResponse {

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

	public static RegionDriverResponse of(Driver driver, Integer duration) {
		return RegionDriverResponse.builder()
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

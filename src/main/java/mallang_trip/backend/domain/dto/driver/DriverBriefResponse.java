package mallang_trip.backend.domain.dto.driver;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.driver.Driver;

@Getter
@Builder
public class DriverBriefResponse {

    private Long driverId;
    private String name;
    private String profileImg;

    public static DriverBriefResponse of(Driver driver){
        return DriverBriefResponse.builder()
            .driverId(driver.getId())
            .name(driver.getUser().getName())
            .profileImg(driver.getUser().getProfileImage())
            .build();
    }
}

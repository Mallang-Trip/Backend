package mallang_trip.backend.domain.driver.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.entity.Driver;

import java.util.List;

@Getter
@Builder
public class DriverBriefResponse {

    private Long driverId;
    private String name;
    private String profileImg;
    private List<String> region;

    public static DriverBriefResponse of(Driver driver){
        return DriverBriefResponse.builder()
            .driverId(driver.getId())
            .name(driver.getUser().getName())
            .profileImg(driver.getUser().getProfileImage())
            .region(driver.getRegion())
            .build();
    }
}

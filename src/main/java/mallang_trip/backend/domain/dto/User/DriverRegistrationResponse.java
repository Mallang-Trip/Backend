package mallang_trip.backend.domain.dto.User;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.user.Driver;

@Builder
@Getter
public class DriverRegistrationResponse {

    private Long id;
    private Long driverId;
    private String licenseImg;
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleCapacity;
    private List<String> regions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DriverRegistrationResponse of(Driver driver) {
        return DriverRegistrationResponse.builder()
            .id(driver.getId())
            .driverId(driver.getDriver().getId())
            .licenseImg(driver.getLicenceImg())
            .vehicleType(driver.getVehicleType())
            .vehicleNumber(driver.getVehicleNumber())
            .vehicleCapacity(driver.getVehicleCapacity())
            .regions(driver.getRegions())
            .createdAt(driver.getCreatedAt())
            .updatedAt(driver.getUpdatedAt())
            .build();
    }
}

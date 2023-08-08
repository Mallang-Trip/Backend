package mallang_trip.backend.domain.dto.driver;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangeVehicleRequest {

    private String vehicleImg;
    private String vehicleModel;
    private String vehicleNumber;
    private Integer vehicleCapacity;
}

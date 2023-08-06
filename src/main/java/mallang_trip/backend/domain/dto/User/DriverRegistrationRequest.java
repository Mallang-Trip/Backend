package mallang_trip.backend.domain.dto.User;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DriverRegistrationRequest {

    private String vehicleType;
    private String vehicleNumber;
    private String vehicleCapacity;
    private List<String> regions;
}

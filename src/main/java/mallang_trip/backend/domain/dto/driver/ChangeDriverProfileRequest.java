package mallang_trip.backend.domain.dto.driver;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangeDriverProfileRequest {

    private String profileImg;
    private String region;
    private List<String> weeklyHolidays;
//    private List<String> holidays;
//    private String phoneNumber;
    private String bank;
    private String accountHolder;
    private String accountNumber;
    List<DriverPriceRequest> prices;
    private String vehicleImg;
    private String vehicleModel;
//    private String vehicleNumber;
    private Integer vehicleCapacity;
}

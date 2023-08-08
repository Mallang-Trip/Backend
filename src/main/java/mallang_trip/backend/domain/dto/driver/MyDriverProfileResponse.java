package mallang_trip.backend.domain.dto.driver;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.constant.Week;
import mallang_trip.backend.domain.dto.course.CourseNameResponse;

@Getter
@Builder
public class MyDriverProfileResponse {

    private Long userId;
    private String name;
    private String vehicleImg;
    private String vehicleModel;
    private String vehicleNumber;
    private String bank;
    private String accountHolder;
    private String accountNumber;
    private String region;
    private List<Week> weeklyHoliday;
    private List<DriverPriceResponse> prices;
    private List<CourseNameResponse> courses;
    private DriverStatus status;
}

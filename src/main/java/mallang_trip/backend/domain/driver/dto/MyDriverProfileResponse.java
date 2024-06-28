package mallang_trip.backend.domain.driver.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.course.dto.CourseBriefResponse;

@Getter
@Builder
public class MyDriverProfileResponse {

    private Long userId;
    private String name;
    private String profileImg;
    private List<String> region;
    private List<DayOfWeek> weeklyHoliday;
    private List<LocalDate> holidays;
    private String vehicleImg;
    private String vehicleModel;
    private String vehicleNumber;
    private Integer vehicleCapacity;
    private String bank;
    private String accountHolder;
    private String accountNumber;
    private String phoneNumber;
    private String introduction;
    private List<DriverPriceResponse> prices;
    private List<CourseBriefResponse> courses;
    private DriverStatus status;
}

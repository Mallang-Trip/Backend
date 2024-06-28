package mallang_trip.backend.domain.driver.dto;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangeDriverProfileRequest {

    // 차량 정보
    @NotBlank
    private String vehicleModel;
    @NotNull
    @Min(value = 1)
    private Integer vehicleCapacity;
    @NotBlank
    private String vehicleNumber;
    @NotBlank
    private String vehicleImg;

    // 활동 가능 지역
    @NotBlank
    private List<String> region;

    // 입금 계좌 & 운행 가격
    @NotBlank
    private String bank;
    @NotBlank
    private String accountHolder;
    @NotBlank
    @Size(min = 10, max = 14)
    private String accountNumber;
    @NotNull
    List<DriverPriceRequest> prices;

    // 프로필 정보
    private String profileImg;
    private List<String> weeklyHolidays;
    private List<String> holidays;
    private String introduction;

    @NotBlank
    private String phoneNumber;
}

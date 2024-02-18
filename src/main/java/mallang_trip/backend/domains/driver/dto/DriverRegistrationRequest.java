package mallang_trip.backend.domains.driver.dto;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.driver.entity.Driver;
import mallang_trip.backend.domains.user.entity.User;

@Builder
@Getter
public class DriverRegistrationRequest {

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
    private String region;

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

    // 서류
    @NotBlank
    private String driverLicenceImg;
    @NotBlank
    private String taxiLicenceImg;
    @NotBlank
    private String insuranceLicenceImg;

    //자기소개
    private String introduction;

    public Driver toDriver(User user){
        return Driver.builder()
            .user(user)
            .vehicleImg(vehicleImg)
            .driverLicenceImg(driverLicenceImg)
            .taxiLicenceImg(taxiLicenceImg)
            .insuranceLicenceImg(insuranceLicenceImg)
            .vehicleModel(vehicleModel)
            .vehicleNumber(vehicleNumber)
            .vehicleCapacity(vehicleCapacity)
            .region(region)
            .bank(bank)
            .accountHolder(accountHolder)
            .accountNumber(accountNumber)
            .introduction(introduction)
            .build();
    }
}

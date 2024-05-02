package mallang_trip.backend.domain.driver.dto;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.user.entity.User;

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

    public String driverInfoEmail(User user){

        // newline for html email
        String newline = "<br>";

        return new StringBuilder()
            .append("이름: ").append(user.getName()).append(newline)
            .append("차량 모델: ").append(vehicleModel).append(newline)
            .append("차량 번호: ").append(vehicleNumber).append(newline)
            .append("최대 탑승 인원: ").append(vehicleCapacity).append(newline)
            .append("활동 가능 지역: ").append(region).append(newline)
            .append("자기소개: ").append(introduction).append(newline)
            .toString();
    }
}

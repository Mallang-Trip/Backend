package mallang_trip.backend.domain.driver.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.entity.Driver;

@Getter
@Builder
public class DriverRegistrationResponse {

    private Long driverId;
    private String vehicleModel;
    private Integer vehicleCapacity;
    private String vehicleNumber;
    private List<String> region;
    private String bank;
    private String accountHolder;
    private String accountNumber;
    private List<DriverPriceResponse> prices;
    private List<String> vehicleImgs;
    private String driverLicenceImg;
    private String taxiLicenceImg;
    private String insuranceLicenceImg;
    private String introduction;
    private DriverStatus status;
    private String profileImg;
    private String phoneNumber;

    public static DriverRegistrationResponse of(Driver driver, List<DriverPriceResponse> prices){
        return DriverRegistrationResponse.builder()
            .driverId(driver.getId())
            .vehicleModel(driver.getVehicleModel())
            .vehicleCapacity(driver.getVehicleCapacity())
            .vehicleNumber(driver.getVehicleNumber())
            .region(driver.getRegion())
            .bank(driver.getBank())
            .accountHolder(driver.getAccountHolder())
            .accountNumber(driver.getAccountNumber())
            .prices(prices)
            .vehicleImgs(driver.getVehicleImgs())
            .driverLicenceImg(driver.getDriverLicenceImg())
            .taxiLicenceImg(driver.getTaxiLicenceImg())
            .insuranceLicenceImg(driver.getInsuranceLicenceImg())
            .introduction(driver.getIntroduction())
            .status(driver.getStatus())
            .profileImg(driver.getUser().getProfileImage())
            .phoneNumber(driver.getUser().getPhoneNumber())
            .build();
    }
}

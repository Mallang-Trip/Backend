package mallang_trip.backend.domain.dto.driver;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.user.Driver;
import mallang_trip.backend.domain.entity.user.User;

@Builder
@Getter
public class DriverRegistrationRequest {

    private String vehicleModel;
    private Integer vehicleCapacity;
    private String vehicleNumber;
    private String region;
    private String bank;
    private String accountHolder;
    private String accountNumber;
    private List<DriverPriceRequest> prices;
    private String vehicleImg;
    private String driverLicenceImg;
    private String taxiLicenceImg;
    private String insuranceLicenceImg;
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

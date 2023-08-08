package mallang_trip.backend.domain.dto.driver;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.user.DriverPrice;

@Getter
@Builder
public class DriverPriceResponse {

    private Integer hours;
    private Integer price;

    public static DriverPriceResponse of(DriverPrice driverPrice){
        return DriverPriceResponse.builder()
            .hours(driverPrice.getHours())
            .price(driverPrice.getPrice())
            .build();
    }
}

package mallang_trip.backend.domain.dto.driver;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverPrice;

@Getter
@Builder
public class DriverPriceRequest {

    private Integer hours;
    private Integer price;

    public DriverPrice toDriverPrice(Driver driver){
        return DriverPrice.builder()
            .driver(driver)
            .hours(hours)
            .price(price)
            .build();
    }
}

package mallang_trip.backend.domain.driver.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.entity.DriverPrice;

@Getter
@Builder
public class DriverPriceRequest {

    @NotNull
    @Max(value = 24)
    @Min(value = 1)
    private Integer hours;

    @NotNull
    @Min(value = 0)
    private Integer price;

    public DriverPrice toDriverPrice(Driver driver){
        return DriverPrice.builder()
            .driver(driver)
            .hours(hours)
            .price(price)
            .build();
    }
}

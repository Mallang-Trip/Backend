package mallang_trip.backend.domain.driver.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.driver.constant.DriverIncomeType;
import mallang_trip.backend.domain.driver.entity.DriverIncome;

@Getter
@Builder
public class DriverIncomeResponse {

	private DriverIncomeType type;
	private Integer amount;
	private LocalDateTime createdAt;

	public static DriverIncomeResponse of(DriverIncome income){
		return DriverIncomeResponse.builder()
			.type(income.getType())
			.amount(income.getAmount())
			.createdAt(income.getCreatedAt())
			.build();
	}
}

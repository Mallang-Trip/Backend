package mallang_trip.backend.domain.income.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyIncomeResponse {

	private String month;
	private Integer income;
}

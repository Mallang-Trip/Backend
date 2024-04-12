package mallang_trip.backend.domain.income.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.income.constant.IncomeType;
import mallang_trip.backend.domain.income.entity.Income;

@Getter
@Builder
public class IncomeResponse {

	private Long incomeId;
	private Long partyId;
	private String partyName;
	private LocalDate date;
	private IncomeType type;
	private Boolean remitted;
	private Integer beforeCommission;
	private Integer commission;
	private Integer afterCommission;

	public static IncomeResponse of(Income income){
		System.out.println(income.getParty().getId() + (income.getParty().getCourse() == null? "null" : "not null"));
		return IncomeResponse.builder()
			.incomeId(income.getId())
			.partyId(income.getParty().getId())
			.partyName(income.getParty().getCourse().getName())
			.date(income.getParty().getEndDate())
			.type(income.getType())
			.remitted(income.getRemitted())
			.beforeCommission(income.getAmount())
			.commission(income.getCommission())
			.afterCommission(income.getAmount() - income.getCommission())
			.build();
	}
}

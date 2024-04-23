package mallang_trip.backend.domain.income.service;

import static mallang_trip.backend.domain.income.constant.IncomeType.PARTY_INCOME;
import static mallang_trip.backend.domain.income.constant.IncomeType.PENALTY_INCOME;
import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.domain.income.constant.IncomeType;
import mallang_trip.backend.domain.income.dto.IncomeResponse;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.income.entity.CommissionRate;
import mallang_trip.backend.domain.income.repository.CommissionRateRepository;
import mallang_trip.backend.domain.income.repository.IncomeRepository;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.income.entity.Income;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeService {

	private final DriverService driverService;
	private final IncomeRepository incomeRepository;
	private final CommissionRateRepository commissionRateRepository;

	/**
	 * 수익금을 저장합니다.
	 *
	 * @param party  수익이 발생한 파티 객체
	 * @param type   수익금 종류 (PARTY_INCOME, PENALTY_INCOME)
	 * @param amount 수익 금액 값
	 */
	public void create(Party party, IncomeType type, Integer amount) {
		incomeRepository.save(Income.builder()
			.party(party)
			.amount(amount)
			.type(type)
			.commission(calculateCommission(type, amount))
			.build());
	}

	/**
	 * 수익의 수수료를 계산합니다.
	 *
	 * @param type 수익 종류
	 * @param amount 전체 수익 금액
	 */
	public int calculateCommission(IncomeType type, Integer amount){
		// 수수료 계산
		CommissionRate rate = commissionRateRepository.findById(1L).get();
		int commission = 0;
		if (type.equals(PARTY_INCOME)) {
			commission = (int) (amount * rate.getPartyCommissionRate());
		} else if (type.equals(PENALTY_INCOME)) {
			commission = (int) (amount * rate.getPenaltyCommissionRate());
		}

		return commission;
	}

	/**
	 * 파티의 위약금을 드라이버의 수익으로 등록합니다.
	 *
	 * @param party 수익으로 등록할 Party 객체
	 */
	public void createPenaltyIncome(Party party) {
		int penalty = party.getCourse().getDiscountPrice();
		if (penalty > 0) {
			create(party, PENALTY_INCOME, penalty);
		}
	}

	/**
	 * 전체 수익 내역을 조회합니다.
	 */
	private List<IncomeResponse> getIncomes() {
		Driver driver = driverService.getCurrentDriver();

		return incomeRepository.findByDriver(driver.getId())
			.stream()
			.map(IncomeResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 송금된 수익 내역을 조회합니다.
	 */
	public List<IncomeResponse> getRemittedIncomes() {
		Driver driver = driverService.getCurrentDriver();

		return incomeRepository.findRemittedIncomesByDriver(driver.getId())
			.stream()
			.map(IncomeResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 월 별 수익금 내역을 조회합니다.
	 *
	 * @param month 조회할 날짜 (YYYY-MM: 월 별 조회, all: 전체 기간 조회)
	 */
	public List<IncomeResponse> getMonthlyIncomes(String month) {
		// 전체 조회 시
		if(month.equalsIgnoreCase("ALL")){
			return getIncomes();
		}
		// YYYY-MM format check
		if (!Pattern.matches("\\d{4}-\\d{2}", month)) {
			throw new BaseException(Bad_Request);
		}

		Driver driver = driverService.getCurrentDriver();
		LocalDate startDate = LocalDate.parse(month + "-01");
		LocalDate endDate = startDate.plusMonths(1);

		return incomeRepository.findByDriverAndPeriod(
			driver.getId(), startDate.toString(), endDate.toString())
			.stream()
			.map(IncomeResponse::of)
			.collect(Collectors.toList());
	}

}

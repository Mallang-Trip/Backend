package mallang_trip.backend.domain.income.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.income.dto.IncomeResponse;
import mallang_trip.backend.domain.income.dto.RemittanceCompleteRequest;
import mallang_trip.backend.domain.income.entity.CommissionRate;
import mallang_trip.backend.domain.income.entity.Income;
import mallang_trip.backend.domain.income.repository.CommissionRateRepository;
import mallang_trip.backend.domain.income.repository.IncomeRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class IncomeAdminService {

	private final IncomeRepository incomeRepository;
	private final CommissionRateRepository commissionRateRepository;
	private final IncomeService incomeService;

	private final Long commissionRateId = 1L;

	/**
	 * (관리자) 월 별 수익 내역을 조회합니다.
	 *
	 * @param month 조회할 날짜 (YYYY-MM: 월 별 조회, all: 전체 기간 조회)
	 */
	public List<IncomeResponse> getIncomesByMonth(String month) {
		// 전체 조회 시
		if (month.equalsIgnoreCase("ALL")) {
			return incomeRepository.findAllOrderByEndDateDesc().stream()
				.map(IncomeResponse::of)
				.collect(Collectors.toList());
		}
		// YYYY-MM format check
		if (!Pattern.matches("\\d{4}-\\d{2}", month)) {
			throw new BaseException(Bad_Request);
		}

		LocalDate startDate = LocalDate.parse(month + "-01");
		LocalDate endDate = startDate.plusMonths(1);

		return incomeRepository.findByPeriod(startDate.toString(), endDate.toString()).stream()
			.map(IncomeResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * (관리자) 수익 내역을 삭제(soft delete)합니다.
	 *
	 * @param incomeId 삭제할 수익에 해당하는 id 값
	 */
	public void deleteIncome(Long incomeId) {
		Income income = incomeRepository.findById(incomeId)
			.orElseThrow(() -> new BaseException(Not_Found));
		incomeRepository.delete(income);
	}

	/**
	 * (관리자) 송금 완료 처리합니다.
	 *
	 * @param incomeId 송금 완료 처리할 Income id 값
	 */
	public void completeRemittance(Long incomeId, RemittanceCompleteRequest request) {
		Income income = incomeRepository.findById(incomeId)
			.orElseThrow(() -> new BaseException(Not_Found));
		income.completeRemittance(request);
	}

	/**
	 * (관리자) 수익 금액을 변경합니다.
	 *
	 * @param incomeId 수익 내역에 해당하는 Income id 값
	 * @param amount 변경할 수익 금액
	 */
	public void changeIncomeAmount(Long incomeId, Integer amount) {
		Income income = incomeRepository.findById(incomeId)
			.orElseThrow(() -> new BaseException(Not_Found));
		income.changeAmount(amount, incomeService.calculateCommission(income.getType(), amount));
	}

	/**
	 * (관리자) 수수료 비율을 변경합니다.
	 *
	 * @param partyCommissionRate   변경할 파티 수수료 비율
	 * @param penaltyCommissionRate 변경할 위약금 수수료 비율
	 */
	public void changeCommissionRate(double partyCommissionRate, double penaltyCommissionRate) {
		CommissionRate rate = commissionRateRepository.findById(commissionRateId).get();
		rate.changeCommissionRate(partyCommissionRate, penaltyCommissionRate);
	}
}

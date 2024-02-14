package mallang_trip.backend.service.driver;

import static mallang_trip.backend.constant.DriverIncomeType.CANCEL_PENALTY;
import static mallang_trip.backend.constant.DriverIncomeType.WITHDRAW;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_DRIVER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.DriverIncomeType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.driver.DriverIncomeResponse;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverIncome;
import mallang_trip.backend.repository.driver.DriverIncomeRepository;
import mallang_trip.backend.repository.driver.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverIncomeService {

	private final DriverService driverService;
	private final DriverRepository driverRepository;
	private final DriverIncomeRepository driverIncomeRepository;

	public void create(Driver driver, DriverIncomeType type, Integer amount){
		if(type.equals(CANCEL_PENALTY) || type.equals(WITHDRAW)){
			amount = -amount;
		}
		driverIncomeRepository.save(DriverIncome.builder()
			.driver(driver)
			.type(type)
			.amount(amount)
			.build());
	}

	/**
	 * 내 수익 내역 보기
	 */
	public List<DriverIncomeResponse> get(){
		return get(driverService.getCurrentDriver());
	}

	/**
	 * 드라이버의 수익 내역 조회
	 */
	public List<DriverIncomeResponse> get(Driver driver){
		return driverIncomeRepository.findByDriverOrderByCreatedAtDesc(driver).stream()
			.map(DriverIncomeResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 드라이버의 총 수익금 조회
	 */
	private Integer getTotalIncome(Driver driver){
		Integer totalIncome = 0;
		for(DriverIncome income : driverIncomeRepository.findByDriverOrderByCreatedAtDesc(driver)){
			totalIncome += income.getAmount();
		}
		return totalIncome;
	}

	/**
	 * (관리자) 수익금 송금 처리
	 */
	public void withdraw(Long driverId, Integer amount){
		Driver driver = driverRepository.findById(driverId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		create(driver, WITHDRAW, amount);
	}
}

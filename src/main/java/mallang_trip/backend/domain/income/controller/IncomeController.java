package mallang_trip.backend.domain.income.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.income.dto.CommissionRateResponse;
import mallang_trip.backend.domain.income.dto.IncomeResponse;
import mallang_trip.backend.domain.income.service.IncomeAdminService;
import mallang_trip.backend.domain.income.service.IncomeService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Income API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

	private final IncomeService incomeService;
	private final IncomeAdminService incomeAdminService;

	@ApiOperation(value = "(드라이버)월 별 수익금 내역 조회")
	@GetMapping("/monthly")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "month", value = "YYYY-MM: 월 별 조회, ALL: 전체 기간 조회", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<List<IncomeResponse>> getMonthlyIncome(@RequestParam("month") String month) throws BaseException {
		return new BaseResponse<>(incomeService.getMonthlyIncomes(month));
	}

	@ApiOperation(value = "(드라이버)송금된 수익금 내역 조회")
	@GetMapping("/remitted")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<List<IncomeResponse>> getRemittedIncomes() throws BaseException {
		return new BaseResponse<>(incomeService.getRemittedIncomes());
	}

	@ApiOperation(value = "수수료 조회")
	@GetMapping("/commission-rate")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 400, message = "잘못된 요청입니다."),
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_USER')") // 사용자
	public BaseResponse<CommissionRateResponse> getCommissionRate() throws BaseException {
		return new BaseResponse<>(incomeAdminService.getCommissionRate());
	}

}

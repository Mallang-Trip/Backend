package mallang_trip.backend.domain.income.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.income.dto.CommissionRateResponse;
import mallang_trip.backend.domain.income.dto.IncomeResponse;
import mallang_trip.backend.domain.income.dto.RemittanceCompleteRequest;
import mallang_trip.backend.domain.income.service.IncomeAdminService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Income Admin API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/income/admin")
public class IncomeAdminController {

	private final IncomeAdminService incomeAdminService;

	@ApiOperation(value = "(관리자)월 별 수익 내역 조회")
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
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<IncomeResponse>> getIncomesByMonth(
		@RequestParam(value = "month") String month) throws BaseException {
		return new BaseResponse<>(incomeAdminService.getIncomesByMonth(month));
	}

	@ApiOperation(value = "(관리자) 수익 삭제")
	@DeleteMapping("/{income_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "income_id", value = "income_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "해당 수익금 내역을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> deleteIncome(@PathVariable("income_id") Long incomeId)
		throws BaseException {
		incomeAdminService.deleteIncome(incomeId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자)송금 완료 처리")
	@PostMapping("/remittance/{income_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "income_id", value = "income_id", required = true, paramType = "path", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "해당 수익금 내역을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> completeRemittance(@PathVariable("income_id") Long incomeId,
		@RequestBody @Valid
		RemittanceCompleteRequest request) throws BaseException {
		incomeAdminService.completeRemittance(incomeId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자)수익 금액 변경")
	@PutMapping("/{income_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "income_id", value = "income_id", required = true, paramType = "path", dataTypeClass = Long.class),
		@ApiImplicitParam(name = "amount", value = "변경할 금액 값", required = true, paramType = "query", dataTypeClass = Integer.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "해당 수익금 내역을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> changeIncome(@PathVariable("income_id") Long incomeId,
		@RequestParam("amount") Integer amount) throws BaseException {
		incomeAdminService.changeIncomeAmount(incomeId, amount);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자)수수료 변경")
	@PutMapping("/commission-rate")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "partyCommissionPercent", value = "파티 수익 수수료 비율(%)", required = true, paramType = "query", dataTypeClass = Double.class),
		@ApiImplicitParam(name = "penaltyCommissionPercent", value = "위약금 수수료 비율(%)", required = true, paramType = "query", dataTypeClass = Double.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> changeCommissionRate(@RequestParam Double partyCommissionPercent,
		@RequestParam Double penaltyCommissionPercent) throws BaseException {
		incomeAdminService.changeCommissionRate(partyCommissionPercent, penaltyCommissionPercent);
		return new BaseResponse<>("성공");
	}

}

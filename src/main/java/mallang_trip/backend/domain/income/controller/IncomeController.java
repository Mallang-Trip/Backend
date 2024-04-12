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
import mallang_trip.backend.domain.article.dto.ArticleIdResponse;
import mallang_trip.backend.domain.article.dto.ArticleRequest;
import mallang_trip.backend.domain.income.dto.IncomeResponse;
import mallang_trip.backend.domain.income.dto.MonthlyIncomeResponse;
import mallang_trip.backend.domain.income.dto.RemittanceCompleteRequest;
import mallang_trip.backend.domain.income.service.IncomeService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Income API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

	private final IncomeService incomeService;

	@ApiOperation(value = "(드라이버)전체 수익금 내역 조회")
	@GetMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<List<IncomeResponse>> getIncomes() throws BaseException {
		return new BaseResponse<>(incomeService.getIncomes());
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

	@ApiOperation(value = "(드라이버)월 별 총 수익금 조회")
	@GetMapping("/monthly")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "month", value = "YYYY-MM", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<MonthlyIncomeResponse> getMonthlyIncome(@RequestParam("month") String month) throws BaseException {
		return new BaseResponse<>(incomeService.getMonthlyIncome(month));
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
	public BaseResponse<String> completeRemittance (@PathVariable("income_id") Long incomeId, @RequestBody @Valid
		RemittanceCompleteRequest request) throws BaseException {
		incomeService.completeRemittance(incomeId, request);
		return new BaseResponse<>("성공");
	}
}

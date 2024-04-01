package mallang_trip.backend.domain.payple.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.payple.dto.CardRequest;
import mallang_trip.backend.domain.payple.dto.CardResponse;
import mallang_trip.backend.domain.payple.service.PaypleService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Card API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/card")
public class PaypleController {

	private final PaypleService paypleService;

	@ApiOperation(value = "카드 등록", notes = "PCD_RST_URL")
	@PostMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "카드 등록에 실패했습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<CardResponse> register(@RequestBody @Valid CardRequest request) throws BaseException {
		return new BaseResponse<>(paypleService.register(request));
	}

	@ApiOperation(value = "카드 조회", notes = "등록된 카드 정보를 조회합니다.")
	@GetMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 404, message = "등록된 카드가 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<CardResponse> get() throws BaseException{
		return new BaseResponse<>(paypleService.get());
	}

	@ApiOperation(value = "카드 삭제", notes = "등록된 카드 정보를 삭제합니다.")
	@DeleteMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 404, message = "등록된 카드가 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> delete() throws BaseException{
		paypleService.delete();
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "결제 재시도", notes = "결제를 실패한 예약에 대한 결제를 재시도합니다.")
	@PostMapping("/{reservation_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "reservation_id", value = "reservation_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "결제에 실패했습니다."),
		@ApiResponse(code = 404, message = "해당 예약이 존재하지 않거나 등록된 카드가 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> retry(@PathVariable(value = "reservation_id") Long reservationId) throws BaseException{
		paypleService.manualBilling(reservationId);
		return new BaseResponse<>("성공");
	}
}

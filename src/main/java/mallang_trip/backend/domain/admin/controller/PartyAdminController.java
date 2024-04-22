package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.PartyPaymentResponse;
import mallang_trip.backend.domain.admin.service.PartyPaymentService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Party Admin API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party/admin")
public class PartyAdminController {

	private final PartyPaymentService partyPaymentService;

	@GetMapping("/payment")
	@ApiOperation(value = "결제 내역 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "status", value = "reserved, finished 중 하나", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 400, message = "잘못된 요청입니다.."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<PartyPaymentResponse>> getPayments(
		@RequestParam("status") String status) throws BaseException {
		return new BaseResponse<>(partyPaymentService.getPartiesByStatus(status));
	}
}

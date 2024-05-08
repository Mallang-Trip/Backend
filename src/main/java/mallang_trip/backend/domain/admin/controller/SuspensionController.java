package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.admin.dto.SuspendingUserResponse;
import mallang_trip.backend.domain.admin.dto.SuspensionRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Suspension API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/suspension")
public class SuspensionController {

	private final SuspensionService suspensionService;

	@PostMapping("/{user_id}")
	@ApiOperation(value = "유저 정지")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "user_id", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
		@ApiResponse(code = 404, message = "해당하는 유저나 신고 정보를 찾을 수 없습니다."),
		@ApiResponse(code = 409, message = "해당 신고에 대한 제재가 이미 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> suspend(@PathVariable(value = "user_id") Long userId,
		@RequestBody SuspensionRequest request) throws BaseException {
		suspensionService.suspend(userId, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/{user_id}")
	@ApiOperation(value = "유저 모든 정지 취소")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "user_id", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
		@ApiResponse(code = 404, message = "해당하는 유저를 찾을 수 없습니다."),
		@ApiResponse(code = 409, message = "해당 신고에 대한 제재가 이미 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> cancel(@PathVariable(value = "user_id") Long userId) throws BaseException {
		suspensionService.cancelSuspension(userId);
		return new BaseResponse<>("성공");
	}

	@GetMapping
	@ApiOperation(value = "정지중인 유저 목록 조회")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<SuspendingUserResponse>> getSuspendingUsers() throws BaseException {
		return new BaseResponse<>(suspensionService.getSuspendingUsers());
	}

	@DeleteMapping("/report/{report_id}")
	@ApiOperation(value = "신고에 대한 정지 취소")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "report_id", value = "report_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> cancelByReport(@PathVariable(value = "report_id") Long reportId) throws BaseException {
		suspensionService.deleteSuspension(reportId);
		return new BaseResponse<>("성공");
	}
}

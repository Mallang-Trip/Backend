package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.admin.SuspendingUserResponse;
import mallang_trip.backend.domain.dto.admin.SuspensionRequest;
import mallang_trip.backend.service.admin.SuspensionService;
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
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> suspend(@PathVariable(value = "user_id") Long userId,
		@RequestBody SuspensionRequest request) throws BaseException {
		suspensionService.suspend(userId, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/{user_id}")
	@ApiOperation(value = "유저 정지 취소")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> cancel(@PathVariable(value = "user_id") Long userId) throws BaseException {
		suspensionService.cancelSuspension(userId);
		return new BaseResponse<>("성공");
	}

	@GetMapping
	@ApiOperation(value = "정지중인 유저 목록 조회")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<SuspendingUserResponse>> getSuspendingUsers() throws BaseException {
		return new BaseResponse<>(suspensionService.getSuspendingUsers());
	}
}

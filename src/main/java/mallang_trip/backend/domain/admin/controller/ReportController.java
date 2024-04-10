package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.*;

import java.util.List;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.*;
import mallang_trip.backend.domain.admin.service.ReportService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Report API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

	private final ReportService reportService;

	@PostMapping
	@ApiOperation(value = "신고하기")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> report(
		@RequestBody ReportRequest request) throws BaseException {
		reportService.create(request);
		return new BaseResponse<>("성공");
	}

	@GetMapping
	@ApiOperation(value = "신고 목록 조회")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<ReportBriefResponse>> getReports() throws BaseException {
		return new BaseResponse<>(reportService.getReports());
	}

	@GetMapping("/{report_id}")
	@ApiOperation(value = "신고 상세 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "report_id", value = "report_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<ReportDetailsResponse> viewReport(
		@PathVariable(value = "report_id") Long reportId) throws BaseException {
		return new BaseResponse<>(reportService.viewReport(reportId));
	}


	@PutMapping("/{report_id}")
	@ApiOperation(value = "완료 처리하기")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "report_id", value = "report_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> complete(
		@PathVariable(value = "report_id") Long reportId) throws BaseException {
		reportService.complete(reportId);
		return new BaseResponse<>("성공");
	}

	/**
	 * 처리 완료된 신고 목록 조회
	 *
	 */
	@GetMapping("/complete")
	@ApiOperation(value = "완료된 신고 목록 조회")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<ReportCompleteBriefResponse>> getCompleteReports() throws BaseException {
		return new BaseResponse<>(reportService.getCompleteReports());
	}

	/**
	 * 처리 완료된 신고 상세 조회
	 *
	 */
	@GetMapping("/complete/{report_id}")
	@ApiOperation(value = "완료된 신고 상세 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "report_id", value = "report_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<ReportCompleteDetailsResponse> viewCompleteReport(
		@PathVariable(value = "report_id") Long reportId) throws BaseException {
		return new BaseResponse<>(reportService.viewCompleteReport(reportId));
	}

}

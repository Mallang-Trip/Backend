package mallang_trip.backend.domains.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.admin.service.ReportService;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.global.io.BaseResponse;
import mallang_trip.backend.domains.admin.dto.ReportBriefResponse;
import mallang_trip.backend.domains.admin.dto.ReportDetailsResponse;
import mallang_trip.backend.domains.admin.dto.ReportRequest;
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
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> report(
		@RequestBody ReportRequest request) throws BaseException {
		reportService.create(request);
		return new BaseResponse<>("성공");
	}

	@GetMapping
	@ApiOperation(value = "신고 목록 조회")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<ReportBriefResponse>> getReports() throws BaseException {
		return new BaseResponse<>(reportService.getReports());
	}

	@GetMapping("/{report_id}")
	@ApiOperation(value = "신고 상세 조회")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<ReportDetailsResponse> viewReport(
		@PathVariable(value = "report_id") Long reportId) throws BaseException {
		return new BaseResponse<>(reportService.viewReport(reportId));
	}


	@PutMapping("/{report_id}")
	@ApiOperation(value = "완료 처리하기")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> complete(
		@PathVariable(value = "report_id") Long reportId) throws BaseException {
		reportService.complete(reportId);
		return new BaseResponse<>("성공");
	}
}

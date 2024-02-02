package mallang_trip.backend.service.admin;

import static mallang_trip.backend.constant.ReportStatus.COMPLETE;
import static mallang_trip.backend.constant.ReportStatus.WAITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.admin.ReportBriefResponse;
import mallang_trip.backend.domain.dto.admin.ReportDetailsResponse;
import mallang_trip.backend.domain.dto.admin.ReportRequest;
import mallang_trip.backend.domain.entity.admin.Report;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.admin.ReportRepository;
import mallang_trip.backend.repository.user.UserRepository;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

	private final UserService userService;
	private final UserRepository userRepository;
	private final ReportRepository reportRepository;

	/**
	 * 신고
	 */
	public void create(ReportRequest request) {
		User reportee = userRepository.findById(request.getReporteeId())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		reportRepository.save(Report.builder()
			.reporter(userService.getCurrentUser())
			.reportee(reportee)
			.content(request.getContent())
			.type(request.getType())
			.targetId(request.getTargetId())
			.build());
	}

	/**
	 * 신고 목록 조회
	 */
	public List<ReportBriefResponse> getReports() {
		List<Report> reports = Stream.concat(
				reportRepository.findByStatusOrderByCreatedAtDesc(WAITING).stream(),
				reportRepository.findByStatusOrderByCreatedAtDesc(COMPLETE).stream())
			.collect(Collectors.toList());
		return reports.stream()
			.map(ReportBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 신고 상세 조회
	 */
	public ReportDetailsResponse viewReport(Long reportId) {
		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return ReportDetailsResponse.of(report);
	}

	/**
	 * 신고 완료 처리
	 */
	public void complete(Long reportId) {
		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BaseException(Not_Found));
		report.setStatus(COMPLETE);
	}

}
